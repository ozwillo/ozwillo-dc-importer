package org.ozwillo.dcimporter.service;

import org.ozwillo.dcimporter.model.FormModel;
import org.ozwillo.dcimporter.model.ListFormsModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonMappingException;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
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
	@Value("${publik.hostName}")
    private String hostName;
	

	private void getForm(String url) throws URISyntaxException{
		
		URI url_finale =  sign_url(url+"?anonymise");
		LOGGER.error("______________________URL get Form ______________________________"+url_finale);
		
		FormModel form = restTemplate.getForObject(url_finale, FormModel.class);
		convertToDCModel(form);
		LOGGER.error(form.toString());

	}
	
	public ListFormsModel[] getPublikForms() throws URISyntaxException, MalformedURLException{

		String initUrl = "https://"+this.hostName+"/api/forms/"+this.formType+"/list?anonymise";

		URI url = sign_url(initUrl);		
		ListFormsModel[] forms = (ListFormsModel[]) restTemplate.getForObject(url, ListFormsModel[].class);
	
		LOGGER.error("Liste des formulaires :");
		
		for(ListFormsModel f : forms){
			LOGGER.error(f.toString());
			getForm(formatUrl(f.getUrl()));
		}
		return forms;
	}
	
	public void convertToDCModel(FormModel form) {
		
		JsonFactory jfactory = new JsonFactory();
		String path = ""; //set the path to your output file exp: /home/DCForm.json

		/*** write to file ***/
		try {
			JsonGenerator jGenerator = jfactory.createGenerator(new File(path), JsonEncoding.UTF8);
			
			jGenerator.writeStartObject(); // {

			jGenerator.writeStringField("citizenreq:displayId",form.getDisplay_id() );
			jGenerator.writeStringField("citizenreq:lastUpdateTime",form.getLast_update_time() );
			jGenerator.writeStringField("citizenreq:displayName",form.getDisplay_name() );
			jGenerator.writeStringField("citizenreq:submissionChannel",form.getSubmission().getChannel() );
			jGenerator.writeBooleanField("citizenreq:submissionBackoffice",form.getSubmission().getBackoffice() );
			jGenerator.writeStringField("citizenreq:url",form.getUrl() );
			jGenerator.writeStringField("citizenreq:familyName",form.getFields().getNom_famille() );
			jGenerator.writeStringField("citizenreq:firstName",form.getFields().getPrenom() );
			jGenerator.writeStringField("citizenreq:phone",form.getFields().getTelephone() );
			jGenerator.writeStringField("citizenreq:receiptTime",form.getReceipt_time() );
			//jGenerator.writeStringField("citizenreq:email",form.getUser().getEmail() );
			//jGenerator.writeStringField("citizenreq:nameID",form.getUser().getNameID()[0] );
			//jGenerator.writeNumberField("citizenreq:userId",form.getUser().getId() );
			//jGenerator.writeStringField("citizenreq:name",form.getUser().getName() );
			jGenerator.writeNumberField("citizenreq:criticalityLevel",form.getCriticality_level() );
			jGenerator.writeStringField("citizenreq:id",form.getId() );
			jGenerator.writeStringField("citizenreq:organization","http://data.ozwillo.com/dc/type/orgfr:Organisation_0/FR/25060187900027" );
			jGenerator.writeStringField("citizenreq:id","demande-de-rendez-vous-avec-un-elu/4" );
			
			
			jGenerator.writeEndObject(); // }

			jGenerator.close();
		} catch (JsonGenerationException e) {

			e.printStackTrace();

		     } catch (JsonMappingException e) {

			e.printStackTrace();

		     } catch (IOException e) {

			e.printStackTrace();

		     }
		
	}
	
	/**
	 * Calculate a signature with sha256
	 * @return
	 */
	public String calculateSignature(String message, String key) {
	    
		try {
			
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
	
	
	public URI sign_url(String url) throws URISyntaxException{
		
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
		
		try {
			String newQuery = "";
			URL parsedUrl = new URL(url);
			
			if(parsedUrl.getQuery() != null){
				newQuery = parsedUrl.getQuery() + "&";
			} else
				newQuery += "?";

			newQuery += "algo="+this.algo+"&timestamp="+URLEncoder.encode(thisMoment, "UTF-8")+"&nonce="+nonce+"&orig="+URLEncoder.encode(this.orig, "UTF-8");
			String signature = calculateSignature(newQuery, this.secret);
			newQuery += "&signature="+signature;
			
			return new URI(parsedUrl.getProtocol()+"://"+parsedUrl.getHost()+parsedUrl.getPath()+"?"+newQuery);
		} 
		catch (MalformedURLException e) {
			LOGGER.error("MalformedURLException : "+e);
			return null;
		} catch (UnsupportedEncodingException e) {
			LOGGER.error("Unsupported encoding exception !?");
			return null;
		}
	}
	
	private String formatUrl(String url_base) throws MalformedURLException{
		
		URL parsedUrl = new URL(url_base);
		String url_finale = "https://"+parsedUrl.getHost()+"/api/forms"+parsedUrl.getPath();
		return url_finale;
	}
	
	
}
