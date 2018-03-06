package org.ozwillo.dcimporter.publik

import com.fasterxml.jackson.databind.ObjectMapper
import org.ozwillo.dcimporter.model.publik.FormModel
import org.springframework.core.io.Resource
import org.springframework.util.FileCopyUtils
import java.io.IOException

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.json.JsonTest
import org.springframework.core.io.ClassPathResource
import org.springframework.test.context.ActiveProfiles

@JsonTest
@ActiveProfiles("test")
class JsonParsingTest {

    private val formResource: Resource = ClassPathResource("/JsonFiles/Form.json")

    @Test
    @Throws(IOException::class)
    fun testJsonFormParsing() {

        val formJson = FileCopyUtils.copyToByteArray(formResource.inputStream)
        val objectMapper = ObjectMapper()
        val formObject = objectMapper.readValue(formJson, FormModel::class.java)

        assertThat(formObject.display_id).isEqualTo("17-4")
        assertThat(formObject.last_update_time).isEqualTo("2017-05-11T09:08:54Z")
        assertThat(formObject.display_name).isEqualTo("Demande de rendez-vous avec un élu - n°17-4")
        assertThat(formObject.submission.channel).isEqualTo("web")
        assertThat(formObject.submission.backoffice).isFalse()
        assertThat(formObject.url).isEqualTo("https://demarches-sve.test-demarches.sictiam.fr/demande-de-rendez-vous-avec-un-elu/4/")
        assertThat(formObject.fields["nom_famille"]).isEqualTo("agent_sictiam")
        assertThat(formObject.fields["prenom"]).isEqualTo("agent_sictiam")
        assertThat(formObject.fields["telephone"]).isEqualTo("0661444444")
        assertThat(formObject.receipt_time).isEqualTo("2017-05-11T09:08:53Z")
        assertThat(formObject.user?.email).isEqualTo("admin@ozwillo-dev.eu")
        assertThat(formObject.user?.nameID?.get(0)).isEqualTo("5c977a7f1d444fa1ab0f777325fdda93")
        assertThat(formObject.user?.id).isEqualTo(3)
        assertThat(formObject.user?.name).isEqualTo("agent_sictiam agent_sictiam")
        assertThat(formObject.criticality_level).isEqualTo(0)
        assertThat(formObject.id).isEqualTo("demande-de-rendez-vous-avec-un-elu/4")
    }
}
