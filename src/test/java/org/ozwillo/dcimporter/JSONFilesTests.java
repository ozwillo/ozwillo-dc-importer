package org.ozwillo.dcimporter;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ozwillo.dcimporter.model.FormModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import org.springframework.core.io.Resource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.FileCopyUtils;

@RunWith(SpringRunner.class)
@JsonTest
public class JSONFilesTests {

	private static final Logger LOGGER = LoggerFactory.getLogger(JSONFilesTests.class);

	@Autowired
    private JacksonTester<FormModel> formModelJson;

	@Value("classpath:/JsonFiles/Form.json")
	private Resource formResource;
	
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
	public void testJsonFormParsing() throws IOException {
		
		byte[] formJson = FileCopyUtils.copyToByteArray(formResource.getInputStream());
		FormModel formModel = this.formModelJson.parseObject(formJson);
		assertThat(formModel.getId()).isEqualTo("demande-de-rendez-vous-avec-un-elu/4");
	}
	
}
