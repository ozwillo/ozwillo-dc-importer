package org.ozwillo.dcimporter.service;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import org.ozwillo.dcimporter.model.FormsModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class FormsService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(FormsService.class);
	
	public String getForms(){
			
		String algo = "sha256";
		String orig = "agent_sictiam";
		String thisMoment = ZonedDateTime.now().format( DateTimeFormatter.ISO_INSTANT );
		LOGGER.error("Time--------------------------------------"+thisMoment);
        
		RestTemplate restTemplate = new RestTemplate();
		String url ="https://demarches-sve.test-demarches.sictiam.fr/api/forms/demande-de-rendez-vous-avec-un-elu/list?qs_initial&algo="+algo+"&timestamp="+thisMoment+"&orig="+orig;
		LOGGER.error("URL--------------------------------------"+url);
		
		FormsModel forms = restTemplate.getForObject(url, FormsModel.class); 
		
		LOGGER.error("Result------------------------------------"+forms.toString());
		
		return forms.toString();
	}
	
	
}
