package org.ozwillo.dcimporter;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ozwillo.dcimporter.model.FormModel;
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

	@Autowired
	private JacksonTester<FormModel> formModelJson;
	
	@Value("classpath:/JsonFiles/Form.json")
	private Resource formResource;
	
	@Test
	public void testJsonFormParsing() throws IOException {
		
		byte[] formJson = FileCopyUtils.copyToByteArray(formResource.getInputStream());
		FormModel formModel = this.formModelJson.parseObject(formJson);
		
		assertThat(formModel.getDisplay_id()).isEqualTo("17-4");
		assertThat(formModel.getLast_update_time()).isEqualTo("2017-05-11T09:08:54Z");
		assertThat(formModel.getDisplay_name()).isEqualTo("Demande de rendez-vous avec un \u00e9lu - n\u00b017-4");
		assertThat(formModel.getSubmission().getChannel()).isEqualTo("web");
		assertThat(formModel.getSubmission().getBackoffice()).isFalse();
		assertThat(formModel.getUrl()).isEqualTo("https://demarches-sve.test-demarches.sictiam.fr/demande-de-rendez-vous-avec-un-elu/4/");
		assertThat(formModel.getFields().getNom_famille()).isEqualTo("agent_sictiam");
		assertThat(formModel.getFields().getPrenom()).isEqualTo("agent_sictiam");
		assertThat(formModel.getFields().getTelephone()).isEqualTo("0661444444");
		assertThat(formModel.getReceipt_time()).isEqualTo("2017-05-11T09:08:53Z");
		assertThat(formModel.getUser().getEmail()).isEqualTo("admin@ozwillo-dev.eu");
		assertThat(formModel.getUser().getNameID()[0]).isEqualTo("5c977a7f1d444fa1ab0f777325fdda93");
		assertThat(formModel.getUser().getId()).isEqualTo(3);
		assertThat(formModel.getUser().getName()).isEqualTo("agent_sictiam agent_sictiam");
		assertThat(formModel.getCriticality_level()).isEqualTo(0);
		assertThat(formModel.getId()).isEqualTo("demande-de-rendez-vous-avec-un-elu/4");
	}
}
