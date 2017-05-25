package org.ozwillo.dcimporter.service;

import org.oasis_eu.spring.datacore.DatacoreClient;
import org.oasis_eu.spring.datacore.model.DCResource;
import org.ozwillo.dcimporter.model.FormModel;
import org.ozwillo.dcimporter.model.ListFormsModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

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

	private final DatacoreClient datacoreClient;
    private final SystemUserService systemUserService;

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
    @Value("${publik.datacore.project}")
    private String datacoreProject;
    @Value("${publik.datacore.model}")
    private String datacoreModel;
    @Value("${publik.datacore.organization}")
    private String datacoreOrganization;
    @Value("${publik.datacore.organizationIri}")
    private String datacoreOrganizationIri;
    @Value("${datacore.baseUri}")
    private String datacoreBaseUri;

    @Autowired
    public PublikService(DatacoreClient datacoreClient, SystemUserService systemUserService) {
        this.datacoreClient = datacoreClient;
        this.systemUserService = systemUserService;
    }

    private FormModel getForm(String url) throws URISyntaxException{
		
		URI url_finale =  sign_url(url+"?anonymise");
		LOGGER.debug("URL get Form {}", url_finale);

        return restTemplate.getForObject(url_finale, FormModel.class);
	}
	
	public void syncPublikForms() throws URISyntaxException, MalformedURLException{

		String initUrl = "https://"+this.hostName+"/api/forms/"+this.formType+"/list?anonymise";

		URI url = sign_url(initUrl);
		LOGGER.debug("Calling Publik at URL {}", url);
		ListFormsModel[] forms = restTemplate.getForObject(url, ListFormsModel[].class);
	
		LOGGER.debug("Liste des formulaires :");
		
		for (ListFormsModel f : forms) {
			LOGGER.debug(f.toString());
			FormModel formModel = getForm(formatUrl(f.getUrl()));
            DCResource dcResource = convertToDCResource(formModel);
            LOGGER.debug(dcResource.toString());
            systemUserService.runAs(() ->
                datacoreClient.saveResource(datacoreProject, dcResource)
            );
        }
	}
	
	private DCResource convertToDCResource(FormModel form) {
		
        DCResource dcResource = new DCResource();

        dcResource.setBaseUri(datacoreBaseUri);
        dcResource.setType(datacoreModel);
        dcResource.setIri(datacoreOrganizationIri + "/" + form.getDisplay_id());

        dcResource.set("citizenreq:displayId",form.getDisplay_id() );
        dcResource.set("citizenreq:lastUpdateTime",form.getLast_update_time() );
        dcResource.set("citizenreq:displayName",form.getDisplay_name() );
        dcResource.set("citizenreq:submissionChannel",form.getSubmission().getChannel() );
        dcResource.set("citizenreq:submissionBackoffice",form.getSubmission().getBackoffice().toString() );
        dcResource.set("citizenreq:url",form.getUrl() );
        dcResource.set("citizenreq:familyName",form.getFields().getNom_famille() );
        dcResource.set("citizenreq:firstName",form.getFields().getPrenom() );
        dcResource.set("citizenreq:phone",form.getFields().getTelephone() );
        dcResource.set("citizenreq:receiptTime",form.getReceipt_time() );
        //dcResource.set("citizenreq:email",form.getUser().getEmail() );
        //dcResource.set("citizenreq:nameID",form.getUser().getNameID()[0] );
        //jGenerator.writeNumberField("citizenreq:userId",form.getUser().getId() );
        //dcResource.set("citizenreq:name",form.getUser().getName() );
        dcResource.set("citizenreq:criticalityLevel",form.getCriticality_level().toString() );
        dcResource.set("citizenreq:id",form.getId() );
        dcResource.set("citizenreq:organization", datacoreOrganization);

        dcResource.set("citizenreqem:familyName", form.getFields().getNom_famille());
        dcResource.set("citizenreqem:firstName", form.getFields().getPrenom());
        dcResource.set("citizenreqem:phone", form.getFields().getTelephone());

        return dcResource;
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
