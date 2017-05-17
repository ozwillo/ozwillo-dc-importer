package org.ozwillo.dcimporter.service;

import org.ozwillo.dcimporter.model.FormModel;
import org.ozwillo.dcimporter.model.ListFormsModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Base64;


@Service
public class PublikService {

	private static final Logger LOGGER = LoggerFactory.getLogger(PublikService.class);
	
	private RestTemplate restTemplate = new RestTemplate();
	
	@Value("${application.formType}")//add the property formType in the application.yml 
	public String formType ;
	
	private ListFormsModel[] forms;
	
	/**
	 * Get the list of forms 
	 * @return
	 */
	public ListFormsModel[] getListForms(){

		String url = "http://localhost:8080/api/forms/"+formType+"/list";
		forms = (ListFormsModel[]) restTemplate.getForObject(url, ListFormsModel[].class);
	
		System.out.println("Liste des formulaires :");
		
		for(ListFormsModel f : forms){
			System.out.println(f.toString());
		}
		return forms;
	}
	
	/**
	 * Get a Form with an Url
	 * @param url
	 * @return
	 */
	public FormModel getForm(String url){
		
		FormModel form = restTemplate.getForObject(url, FormModel.class);
		LOGGER.info(form.toString());
		System.out.println(form.toString());
	
		return form;
	}
	
	public ListFormsModel[] getPublikListForms(){

		String algo = "sha256";
		String orig = "ozwillo-dcimporter";
		String thisMoment = ZonedDateTime.now().format( DateTimeFormatter.ISO_INSTANT );
		thisMoment = thisMoment.substring(0,thisMoment.indexOf("."))+"Z";
		
		String url ="https://demarches-sve.test-demarches.sictiam.fr/api/forms/demande-de-rendez-vous-avec-un-elu/list?algo="+algo+"&timestamp="+thisMoment+"&orig="+orig+"&signature="+this.calculateSignature();
		
		forms = (ListFormsModel[]) restTemplate.getForObject(url, ListFormsModel[].class);
	
		System.out.println("Liste des formulaires :");
		
		for(ListFormsModel f : forms){
			System.out.println(f.toString());
		}
		return forms;
	}
	
	
	public String calculateSignature() {
	    
		try {
			
			String orig = "ozwillo-dcimporter";
			String thisMoment = ZonedDateTime.now().format( DateTimeFormatter.ISO_INSTANT );
			thisMoment = thisMoment.substring(0,thisMoment.indexOf("."))+"Z";
		    
			String message ="https://demarches-sve.test-demarches.sictiam.fr/api/forms/demande-de-rendez-vous-avec-un-elu/list?algo=HASH&timestamp="+thisMoment+"&orig="+orig;			
		    String secret = "aSYZexOBIzl8";
	
		     Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
		     SecretKeySpec secret_key = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
		     sha256_HMAC.init(secret_key);
	
		     String hash = Base64.encodeBase64String(sha256_HMAC.doFinal(message.getBytes()));
		     System.out.println(hash);
		     return hash;
	    }
	    catch (Exception e){
	    	e.printStackTrace();
			return null;
	    }
	   
	}
	
}
