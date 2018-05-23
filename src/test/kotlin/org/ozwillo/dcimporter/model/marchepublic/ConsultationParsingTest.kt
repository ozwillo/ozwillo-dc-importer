package org.ozwillo.dcimporter.model.marchepublic

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.json.JsonTest
import org.springframework.boot.test.json.JacksonTester
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.io.IOException
import java.time.LocalDateTime

@ExtendWith(SpringExtension::class)
@JsonTest
@ActiveProfiles("test")
class ConsultationParsingTest {

    @Autowired
    private lateinit var jacksonTester: JacksonTester<Consultation>

    @Test
    @Throws(IOException::class)
    fun `Test a consultation is correctly deserialized`() {
        val consultationJson = """
            {
                "reference": "reference",
                "objet": "mon marché public",
                "datePublication": "2018-05-01T00:00:00",
                "dateCloture": "2018-05-31T00:00:00",
                "finaliteMarche": "MARCHE",
                "typeMarche": "PRIVE",
                "typePrestation": "TRAVAUX",
                "departementsPrestation": [6,83],
                "passation": "Passation",
                "informatique": "true",
                "emails": ["dev@sictiam.fr", "demat@sictiam.fr"],
                "enLigne": "false",
                "alloti": "true",
                "invisible": "false",
                "nbLots": "1"
            }
            """
        val consultation = jacksonTester.parseObject(consultationJson)

        assertThat(consultation.reference).isEqualTo("reference")
        assertThat(consultation.datePublication).isEqualTo(LocalDateTime.of(2018,5,1,0,0,0))
        assertThat(consultation.finaliteMarche).isEqualTo(FinaliteMarcheType.MARCHE)
        assertThat(consultation.typeMarche).isEqualTo(TypeMarcheType.PRIVE)
        assertThat(consultation.typePrestation).isEqualTo(TypePrestationType.TRAVAUX)
        assertThat(consultation.departementsPrestation.size).isEqualTo(2)
        assertThat(consultation.departementsPrestation.contains(6)).isTrue()
        assertThat(consultation.informatique).isTrue()
        assertThat(consultation.emails.size).isEqualTo(2)
        assertThat(consultation.enLigne).isFalse()
        assertThat(consultation.nbLots).isEqualTo(1)
        assertThat(consultation.alloti).isTrue()
    }
}