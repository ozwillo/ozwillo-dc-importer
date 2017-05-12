package org.ozwillo.dcimporter.controller;


import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


@RestController
public class TestController {

	
	@RequestMapping(value = "/api/ListForms", method = RequestMethod.GET)
    public JSONArray getListForms() throws IOException, ParseException {
		
		String filePath = "/home/medhi/form.json";//path to the json file
		JSONParser parser = new JSONParser();
		 
		try {
			Object obj = parser.parse(new FileReader(filePath));
		
			JSONArray jsonObject = (JSONArray) obj;
			return jsonObject;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}	
    }
	
	@RequestMapping(value = "/api/form", method = RequestMethod.GET)
    public JSONObject getForm() throws IOException, ParseException {
		
		String filePath = "/home/medhi/file.json";//path to the json file
		JSONParser parser = new JSONParser();
		 
		try {
			Object obj = parser.parse(new FileReader(filePath));
		
			JSONObject jsonObject = (JSONObject) obj;
			return jsonObject;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}	
		
        
    }

}
