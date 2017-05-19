package org.ozwillo.dcimporter;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.junit.Test;
import org.ozwillo.dcimporter.model.FormModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.core.io.ClassPathResource;


import com.fasterxml.jackson.databind.ObjectMapper;

@JsonTest
public class JSONFilesTests {

	private static final Logger LOGGER = LoggerFactory.getLogger(JSONFilesTests.class);

    private JacksonTester<FormModel> json;
	
	/*@Test
	public void testJSONListForms() throws Exception{
			
		String filePath = "/JsonFiles/ListForms.json";//path to the json file
		FileReader file = new FileReader( new ClassPathResource(filePath).getFile());
		ObjectMapper mapper = new ObjectMapper();
		
		try {
			
			ListFormsModel[] forms = mapper.readValue(file, ListFormsModel[].class);
			assertThat(forms).isNotNull();
			assertThat(forms).doesNotContainNull();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}*/

	@Test
	public void testJSONFiles() throws IOException{
		
		String filePath = "/JsonFiles/Form.json";//path to the json file

		try {
			FileReader file = new FileReader( new ClassPathResource(filePath).getFile());
			ObjectMapper mapper = new ObjectMapper();	
			FormModel form = mapper.readValue(file, FormModel.class);
			StringBuilder builder = new StringBuilder();
			BufferedReader reader = new BufferedReader(file);
	        String line =null;

	         while ((line = reader.readLine()) != null){
	               builder.append(line);
	         }
	         assertThat(this.json.parse(builder.toString())).isEqualTo(form);
	         reader.close(); 
	         
		} catch (FileNotFoundException e) {
			LOGGER.error("Exception in the testJSONFiles method : "+e);
		}	

	}
	
}
