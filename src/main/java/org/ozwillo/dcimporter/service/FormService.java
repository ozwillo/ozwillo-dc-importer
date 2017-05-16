package org.ozwillo.dcimporter.service;

import org.ozwillo.dcimporter.model.FormModel;
import org.ozwillo.dcimporter.model.ListFormsModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class FormService {

	private static final Logger LOGGER = LoggerFactory.getLogger(FormService.class);
	
	private RestTemplate restTemplate = new RestTemplate();
	
	@Value("${application.formType}")//add the property formType in the application.yml 
	public String formType ;
	
	private ListFormsModel[] forms;
	
	public ListFormsModel[] getListForms(){

		String url = "http://localhost:8080/api/forms/"+formType+"/list";
		forms = (ListFormsModel[]) restTemplate.getForObject(url, ListFormsModel[].class);
	
		System.out.println("Liste des formulaires :");
		
		for(ListFormsModel f : forms){
			System.out.println(f.toString());
		}
		return forms;
	}
	
	public FormModel getForm(String url){
		
		FormModel form = restTemplate.getForObject(url, FormModel.class);
		LOGGER.info(form.toString());
		System.out.println(form.toString());
	
		return form;
	}
	
}
