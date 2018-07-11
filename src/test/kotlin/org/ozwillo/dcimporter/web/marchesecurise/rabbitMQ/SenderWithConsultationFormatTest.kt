package org.ozwillo.dcimporter.web.marchesecurise.rabbitMQ

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.ozwillo.dcimporter.config.DatacoreProperties
import org.ozwillo.dcimporter.model.marchepublic.Consultation
import org.ozwillo.dcimporter.model.marchepublic.FinaliteMarcheType
import org.ozwillo.dcimporter.model.marchepublic.TypeMarcheType
import org.ozwillo.dcimporter.model.marchepublic.TypePrestationType
import org.ozwillo.dcimporter.service.DatacoreService
import org.ozwillo.dcimporter.service.marchesecurise.rabbitMQ.SenderMS
import org.springframework.amqp.AmqpException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

import java.time.LocalDateTime
import java.util.Arrays


@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class SenderWithConsultationFormatTest {

    @Autowired
    internal var sender: SenderMS? = null

    private val SIRET = "123456789"
    private val REFERENCE = "ref-consultation"
    private val BEARER = "eyJpZCI6IjgxZTEzZTBiLWVlYjktNDgyOC05NWM4LWJmNmJlNjAyNTA1Yy9lQkQ0aVRiX1lOVUdqOXZwbDdPbXlBIiwiaWF0IjoxNTMxMTQ5NzkwLjg4OTAwMDAwMCwiZXhwIjoxNTMxMTUzMzkwLjg4OTAwMDAwMH0"


    //TODO:Modifier paramètre JsonConverter, SenderMS et ReciverMS pour passer Consultation au lieu de DCRessourceLight

    @Test
    @Throws(InterruptedException::class)
    fun send_ConsultationFormatJson_by_rabbitMQ() {

        val reference = REFERENCE
        val objet = "test objet tatata"
        val datePublication = LocalDateTime.now()
        val dateCloture = LocalDateTime.now()
        val finaliteMarche = FinaliteMarcheType.AUTRE
        val typeMarche = TypeMarcheType.AUTRE
        val typePrestation = TypePrestationType.AUTRES
        val departementsPrestation = Arrays.asList(74, 38, 69, 6)
        val passation = "test passation"
        val informatique = true
        val passe = "test passe"
        val emails = Arrays.asList("test@test.com")
        val enLigne = true
        val alloti = true
        val invisible = true
        val nbLots = 0

        val consultation = Consultation(reference, objet, datePublication, dateCloture, finaliteMarche, typeMarche, typePrestation, departementsPrestation, passation, informatique, passe, emails, enLigne, alloti, invisible, nbLots)

        /*try {
            sender!!.send(consultation, "consultation", "create")
        } catch (e: AmqpException) {
            e.message
        }*/

    }
}