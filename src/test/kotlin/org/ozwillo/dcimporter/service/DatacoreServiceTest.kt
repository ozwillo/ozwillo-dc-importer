package org.ozwillo.dcimporter.service

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.ozwillo.dcimporter.config.DatacoreProperties
import org.ozwillo.dcimporter.model.marchepublic.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.LocalDateTime
import java.util.*

@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class DatacoreServiceTest(@Autowired val datacoreProperties: DatacoreProperties,
                          @Autowired val datacoreService: DatacoreService) {

    companion object {

        private val MP_PROJECT = "marchepublic_0"
        private val CONSULTATION_TYPE = "marchepublic:consultation_0"
        private val LOT_TYPE = "marchepublic:lot_0"
    }

    private val siret = "123456789"

    private val bearer = "bearer"


    @Test
    fun saveResourceTest() {
        val reference = "ref-consultation-0024"
        val consultation = Consultation(reference = reference,
                objet = "mon marche", datePublication = LocalDateTime.now(), dateCloture = LocalDateTime.now(),
                finaliteMarche = FinaliteMarcheType.MARCHE, typeMarche = TypeMarcheType.PUBLIC,
                typePrestation = TypePrestationType.FOURNITURES, departementsPrestation = listOf(6, 83),
                passation = "passation", informatique = true, passe = "motdepasse", emails = listOf("dev@sictiam.fr", "demat@sictiam.fr"),
                enLigne = false, alloti = false, invisible = false, nbLots = 1)
        val dcConsultation = consultation.toDcObject(datacoreProperties.baseUri, siret)

        datacoreService.saveResource(MP_PROJECT, CONSULTATION_TYPE, dcConsultation, bearer)
    }

    @Test
    fun  updateResourceTest(){
        val reference = "ref-consultation-0013"
        val consultation = Consultation(reference = reference,
                objet = "mon marche modifié", datePublication = LocalDateTime.now(), dateCloture = LocalDateTime.now(),
                finaliteMarche = FinaliteMarcheType.MARCHE, typeMarche = TypeMarcheType.PUBLIC,
                typePrestation = TypePrestationType.FOURNITURES, departementsPrestation = listOf(6, 83),
                passation = "passation modifiée", informatique = true, passe = "motdepasse", emails = listOf("dev@sictiam.fr", "demat@sictiam.fr"),
                enLigne = false, alloti = false, invisible = false, nbLots = 1)
        val dcConsultation = consultation.toDcObject(datacoreProperties.baseUri, siret)

        datacoreService.updateResource(MP_PROJECT, CONSULTATION_TYPE, dcConsultation, bearer)
    }

    @Test
    fun deleteResourceTest(){
        val reference = "ref-consultation-0024"
        val consultation = Consultation(reference = reference,
                objet = "mon marche modifié", datePublication = LocalDateTime.now(), dateCloture = LocalDateTime.now(),
                finaliteMarche = FinaliteMarcheType.MARCHE, typeMarche = TypeMarcheType.PUBLIC,
                typePrestation = TypePrestationType.FOURNITURES, departementsPrestation = listOf(6, 83),
                passation = "passation modifiée", informatique = true, passe = "motdepasse", emails = listOf("dev@sictiam.fr", "demat@sictiam.fr"),
                enLigne = false, alloti = false, invisible = false, nbLots = 1)
        val dcConsultationIri = (consultation.toDcObject(datacoreProperties.baseUri, siret)).getIri()

        datacoreService.deleteResource(MP_PROJECT, CONSULTATION_TYPE, dcConsultationIri, bearer)
    }

    @Test
    fun saveLotResourceTest(){
        val reference = "ref-consultation-001"
        val lot = Lot(uuid = UUID.randomUUID().toString(), libelle = "Libellé Lot", ordre = 1, numero = 1)
        val dcLot = lot.toDcObject(datacoreProperties.baseUri, siret, reference)

        datacoreService.saveResource(MP_PROJECT, LOT_TYPE, dcLot, bearer)
    }
}