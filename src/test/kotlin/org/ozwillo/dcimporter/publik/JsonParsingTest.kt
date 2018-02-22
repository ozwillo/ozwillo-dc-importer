package org.ozwillo.dcimporter.publik

import org.ozwillo.dcimporter.model.publik.FormModel
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.json.JacksonTester
import org.springframework.core.io.Resource
import org.springframework.util.FileCopyUtils
import java.io.IOException

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.autoconfigure.json.JsonTest
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@SpringBootTest
@JsonTest
@ActiveProfiles("test")
class JsonParsingTest {

    @Autowired
    private lateinit var formModelJson: JacksonTester<FormModel>

    @Value("classpath:/JsonFiles/Form.json")
    private lateinit var formResource: Resource

    @Test
    @Throws(IOException::class)
    fun testJsonFormParsing() {

        val formJson = FileCopyUtils.copyToByteArray(formResource.inputStream)
        val (display_id, last_update_time, display_name, submission, url, fields, receipt_time, user, criticality_level, id) = this.formModelJson.parseObject(formJson)

        assertThat(display_id).isEqualTo("17-4")
        assertThat(last_update_time).isEqualTo("2017-05-11T09:08:54Z")
        assertThat(display_name).isEqualTo("Demande de rendez-vous avec un \u00e9lu - n\u00b017-4")
        assertThat(submission.channel).isEqualTo("web")
        assertThat(submission.backoffice).isFalse()
        assertThat(url).isEqualTo("https://demarches-sve.test-demarches.sictiam.fr/demande-de-rendez-vous-avec-un-elu/4/")
        assertThat(fields["nom_famille"]).isEqualTo("agent_sictiam")
        assertThat(fields["prenom"]).isEqualTo("agent_sictiam")
        assertThat(fields["telephone"]).isEqualTo("0661444444")
        assertThat(receipt_time).isEqualTo("2017-05-11T09:08:53Z")
        assertThat(user.email).isEqualTo("admin@ozwillo-dev.eu")
        assertThat(user.nameID[0]).isEqualTo("5c977a7f1d444fa1ab0f777325fdda93")
        assertThat(user.id).isEqualTo(3)
        assertThat(user.name).isEqualTo("agent_sictiam agent_sictiam")
        assertThat(criticality_level).isEqualTo(0)
        assertThat(id).isEqualTo("demande-de-rendez-vous-avec-un-elu/4")
    }
}
