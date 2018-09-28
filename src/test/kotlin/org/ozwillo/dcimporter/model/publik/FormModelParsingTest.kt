package org.ozwillo.dcimporter.model.publik

import org.springframework.core.io.Resource
import org.springframework.util.FileCopyUtils
import java.io.IOException

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.json.JsonTest
import org.springframework.boot.test.json.JacksonTester
import org.springframework.core.io.ClassPathResource
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@JsonTest
@ActiveProfiles("test")
class FormModelParsingTest {

    @Autowired
    private lateinit var jacksonTester: JacksonTester<FormModel>

    @Test
    @Throws(IOException::class)
    fun testJsonFormParsing() {

        val formResource: Resource = ClassPathResource("/data/citizenreq/Form.json")
        val formModelJson = FileCopyUtils.copyToByteArray(formResource.inputStream)
        val formModel = jacksonTester.parseObject(formModelJson)

        assertThat(formModel.display_id).isEqualTo("17-4")
        assertThat(formModel.last_update_time).isEqualTo("2017-05-11T09:08:54Z")
        assertThat(formModel.display_name).isEqualTo("Demande de rendez-vous avec un élu - n°17-4")
        assertThat(formModel.submission.channel).isEqualTo("web")
        assertThat(formModel.submission.backoffice).isFalse()
        assertThat(formModel.url).isEqualTo("https://demarches-sve.test-demarches.sictiam.fr/demande-de-rendez-vous-avec-un-elu/4/")
        assertThat(formModel.fields["nom_famille"]).isEqualTo("agent_sictiam")
        assertThat(formModel.fields["prenom"]).isEqualTo("agent_sictiam")
        assertThat(formModel.fields["telephone"]).isEqualTo("0661444444")
        assertThat(formModel.receipt_time).isEqualTo("2017-05-11T09:08:53Z")
        assertThat(formModel.user?.email).isEqualTo("admin@ozwillo-dev.eu")
        assertThat(formModel.user?.nameID?.get(0)).isEqualTo("5c977a7f1d444fa1ab0f777325fdda93")
        assertThat(formModel.user?.id).isEqualTo(3)
        assertThat(formModel.user?.name).isEqualTo("agent_sictiam agent_sictiam")
        assertThat(formModel.criticality_level).isEqualTo(0)
        assertThat(formModel.id).isEqualTo("demande-de-rendez-vous-avec-un-elu/4")
    }
}
