package org.ozwillo.dcimporter.controller;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


@RestController
@RequestMapping(value = "/api/forms")
public class TestAPIController {

	
	@RequestMapping(value = "/demande-de-rendez-vous-avec-un-elu/list", method = RequestMethod.GET)
    public JSONArray getListForms() throws IOException, ParseException {
		
		String filePath = "/JsonFiles/ListForms.json";//path to the json file
		JSONParser parser = new JSONParser();
		FileReader file = new FileReader( new ClassPathResource(filePath).getFile());

		try {
			Object obj = parser.parse(file);
		
			JSONArray jsonObject = (JSONArray) obj;
			return jsonObject;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}	
    }
	
	@RequestMapping(value = "/form", method = RequestMethod.GET)
    public JSONObject getForm() throws IOException, ParseException {
		
		String filePath = "/JsonFiles/Form.json";//path to the json file
		JSONParser parser = new JSONParser();
		FileReader file = new FileReader( new ClassPathResource(filePath).getFile());
		 
		try {
			Object obj = parser.parse(file);
		
			JSONObject jsonObject = (JSONObject) obj;
			return jsonObject;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}	
		
        
    }

}
