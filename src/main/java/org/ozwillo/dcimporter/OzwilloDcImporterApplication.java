package org.ozwillo.dcimporter;


import org.ozwillo.dcimporter.model.FormModel;
//import java.io.FileReader;
//import org.json.*;
//import org.json.JSONObject;
import org.ozwillo.dcimporter.model.ListFormsModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
//import org.springframework.boot.json.JsonParser;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.web.client.RestTemplate;

//import net.minidev.json.parser.JSONParser;


@SpringBootApplication
@EnableScheduling
@EnableWebSecurity
@ComponentScan(basePackages = {"org.oasis_eu.spring","org.ozwillo.dcimporter"})

public class OzwilloDcImporterApplication {

	private static final Logger LOGGER = LoggerFactory.getLogger(OzwilloDcImporterApplication.class);
	
	public static void main(String[] args) {
		SpringApplication.run(OzwilloDcImporterApplication.class, args);
		
		RestTemplate restTemplate = new RestTemplate();
		ListFormsModel[] forms = (ListFormsModel[]) restTemplate.getForObject("http://localhost:8080/api/ListForms", ListFormsModel[].class);

		for(ListFormsModel f : forms){
			LOGGER.info(f.toString());
			System.out.println(f.toString());
		}
		
		FormModel form = restTemplate.getForObject("http://localhost:8080/api/form", FormModel.class);
		LOGGER.info(form.toString());
		System.out.println(form.toString());
		}
}
