package org.ozwillo.dcimporter.service;

import org.ozwillo.dcimporter.model.FormModel;
import org.ozwillo.dcimporter.model.ListFormsModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.Random;
import java.util.TimeZone;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import org.apache.commons.codec.binary.Base64;


@Service
public class PublikService {

	private static final Logger LOGGER = LoggerFactory.getLogger(PublikService.class);
	
	private RestTemplate restTemplate = new RestTemplate();
	
	@Value("${publik.formType}")
	private String formType;
	@Value("${publik.algo}")
	private String algo;
	@Value("${publik.orig}")
	private String orig ;
	@Value("${publik.secret}")
    private String secret;
	
	
	/**
	 * Get the list of forms 
	 * @return
	 */
	public ListFormsModel[] getListForms(){
		
		ListFormsModel[] forms;

		String url = "http://localhost:8080/api/forms/"+formType+"/list";
		forms = (ListFormsModel[]) restTemplate.getForObject(url, ListFormsModel[].class);
	
		LOGGER.debug("Liste des formulaires :");
		
		for(ListFormsModel form : forms){
			LOGGER.debug(form.toString());
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
		LOGGER.debug(form.toString());
	
		return form;
	}
	
	/**
	 * Get a list of forms from publik
	 * @return
	 */
	public void getPublikListForms(){

		ListFormsModel[] forms;
		String initUrl = "https://demarches-sve.test-demarches.sictiam.fr/api/forms/demande-de-rendez-vous-avec-un-elu/list?anonymise";

		String url = sign_url(initUrl);
		LOGGER.error("---URL---"+url);
		
		forms = (ListFormsModel[]) restTemplate.getForObject(url, ListFormsModel[].class);
	
		LOGGER.debug("Liste des formulaires :");
		
		for(ListFormsModel f : forms){
			LOGGER.debug(f.toString());
		}
	}
	
	/**
	 * Calculate a signature with sha256
	 * @return
	 */
	public String calculateSignature(String message, String key) {
	    
		try {
		     LOGGER.error("message : "+message);
			
		     Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
		     SecretKeySpec secret_key = new SecretKeySpec(key.getBytes(), "HmacSHA256");
		     sha256_HMAC.init(secret_key);
	
		     String hash = Base64.encodeBase64String(sha256_HMAC.doFinal(message.getBytes()));
		     LOGGER.debug("Signature : "+hash);

			hash = URLEncoder.encode(hash, "UTF-8");
		     LOGGER.debug("URL encoded hash : " + hash);

		     return hash;
	    }
	    catch (Exception e){
	    	LOGGER.error("Exception when calculate the signature : "+e);
			return null;
	    }
	   
	}
	
	
	public String sign_url(String url){
		
		TimeZone tz = TimeZone.getTimeZone("UTC");
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		df.setTimeZone(tz);
		String thisMoment = df.format(new Date());
		
		Random random = new Random();
		   // create byte array
		   byte[] nbyte = new byte[16]; 
		   // put the next byte in the array
		   random.nextBytes(nbyte);
		String nonce = DatatypeConverter.printHexBinary(nbyte);
		LOGGER.error("-----------------Time---------------"+thisMoment);
		LOGGER.error("-----------------nonce---------------"+nonce);

		
		try {
			String newQuery = "";
			URL parsedUrl = new URL(url);
			LOGGER.error("-----------------Query---------------"+parsedUrl.getQuery());
			
			if(parsedUrl.getQuery() != null){
				newQuery = parsedUrl.getQuery() + "&";
			} else
				newQuery += "?";

			newQuery += "algo="+this.algo+"&timestamp="+thisMoment+"&nonce="+nonce+"&orig="+URLEncoder.encode(this.orig, "UTF-8");
			LOGGER.debug("Signing : " + newQuery);
			String signature = calculateSignature(newQuery, this.secret);
			newQuery += "&signature="+signature;
			
			return url + newQuery.replaceFirst(parsedUrl.getQuery(), "");
		} 
		catch (MalformedURLException e) {
			LOGGER.error("MalformedURLException : "+e);
			return null;
		} catch (UnsupportedEncodingException e) {
			LOGGER.error("Unsupported encoding exception !?");
			return null;
		}
	}
	
	
}
