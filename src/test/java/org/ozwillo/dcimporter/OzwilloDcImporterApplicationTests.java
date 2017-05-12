package org.ozwillo.dcimporter;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ozwillo.dcimporter.model.FormModel;
import org.ozwillo.dcimporter.model.ListFormsModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

@RunWith(SpringRunner.class)
@SpringBootTest
public class OzwilloDcImporterApplicationTests {

	private static final Logger LOGGER = LoggerFactory.getLogger(OzwilloDcImporterApplicationTests.class);
	
	@Test
	public void contextLoads() {
		
		
		RestTemplate restTemplate = new RestTemplate();
		ListFormsModel[] forms = (ListFormsModel[]) restTemplate.getForObject("http://localhost:8080/api/forms", ListFormsModel[].class);

		for(ListFormsModel f : forms){
			LOGGER.info(f.toString());
			System.out.println(f.toString());
		}
		
		FormModel form = restTemplate.getForObject("http://localhost:8080/api/form", FormModel.class);
		LOGGER.info(form.toString());
		System.out.println(form.toString());

	}

}
