package org.ozwillo.dcimporter.service

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.ozwillo.dcimporter.config.DatacoreProperties
import org.ozwillo.dcimporter.model.datacore.DCBusinessResourceLight
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
        private val PIECE_TYPE = "marchepublic:piece_0"
    }

    private val siret = "123456789"

    private val bearer = "bearer"


    @Test
    fun saveResourceTest() {
        val reference = "ref-consultation-00060"
        val consultation = Consultation(reference = reference,
                objet = "mon marche", datePublication = LocalDateTime.now(), dateCloture = LocalDateTime.now(),
                finaliteMarche = FinaliteMarcheType.MARCHE, typeMarche = TypeMarcheType.PUBLIC,
                typePrestation = TypePrestationType.FOURNITURES, departementsPrestation = listOf(6, 83),
                passation = "AORA", informatique = true, passe = "motdepasse", emails = listOf("dev@sictiam.fr", "demat@sictiam.fr"),
                enLigne = false, alloti = false, invisible = false, nbLots = 1)
        val dcConsultation = consultation.toDcObject(datacoreProperties.baseUri, siret)

        datacoreService.saveResource(MP_PROJECT, CONSULTATION_TYPE, dcConsultation, bearer)
    }

    @Test
    fun  updateResourceTest(){
        val reference = "ref-consultation-00055"
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
        val reference = "ref-consultation-00051"
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
        val reference = "ref-consultation-00061"
        val lot = Lot(uuid = UUID.randomUUID().toString(), libelle = "Lot 1", ordre = 1, numero = 1)
        val dcLot = lot.toDcObject(datacoreProperties.baseUri, siret, reference)

        datacoreService.saveResource(MP_PROJECT, LOT_TYPE, dcLot, bearer)
    }

    @Test
    fun updateLotResourceTest(){
        val reference = "ref-consultation-00053"
        val lot = Lot(uuid = "92523230-658e-4a60-9f8b-3faf9a7d93c3", libelle = "Lot modifié", ordre = 3, numero = 3)
        val dcLot = lot.toDcObject(datacoreProperties.baseUri, siret, reference)

        datacoreService.updateResource(MP_PROJECT, LOT_TYPE, dcLot, bearer)
    }

    @Test
    fun deleteLotResourceTest(){
        val reference = "ref-consultation-00061"
        val lot = Lot(uuid = "45bfdff5-aa50-4786-9ff7-7d83e1525f38", libelle = "Lot modifié", ordre = 3, numero = 3)
        val dcLotIri = lot.toDcObject(datacoreProperties.baseUri, siret, reference).getIri()

        datacoreService.deleteResource(MP_PROJECT, LOT_TYPE, dcLotIri, bearer)
    }

    @Test
    fun savePieceResourceTest(){
        val reference = "ref-consultation-00058"
        val piece = Piece(uuid = UUID.randomUUID().toString(), uuidLot = null, libelle = "Libellé Piece 11", aapc = false, ordre = 3, nom = "FichierTest11", extension = "txt", contenu = "Hello world again !".toByteArray(), poids = 60000)

        val dcPiece:DCBusinessResourceLight = piece.toDcObject(datacoreProperties.baseUri, siret, reference)

        datacoreService.saveResource(MP_PROJECT, PIECE_TYPE, dcPiece, bearer)
    }

    @Test
    fun updatePieceResourceTest(){
        val reference = "ref-consultation-00012"
        val piece = Piece(uuid = "99030146-0410-472b-a4dc-566255d4a2cb", uuidLot = null, libelle = "Libellé modifié pour pièce 3", aapc = false, ordre = 4, nom = "FichierTest03", extension = "txt", contenu = "Hello world again !".toByteArray(), poids = 10)
        val dcPiece:DCBusinessResourceLight = piece.toDcObject(datacoreProperties.baseUri, siret, reference)

        datacoreService.updateResource(MP_PROJECT, PIECE_TYPE, dcPiece, bearer)

    }

    @Test
    fun deletePieceResourceTest(){
        val reference = "ref-consultation-00058"
        val piece = Piece(uuid = "133477bc-6005-48d8-a2c7-e971ac9c5ba8", uuidLot = null, libelle = "Libellé modifié pour pièce 3", aapc = false, ordre = 4, nom = "FichierTest03", extension = "txt", contenu = "Hello world again !".toByteArray(), poids = 10)
        val dcPieceIri = piece.toDcObject(datacoreProperties.baseUri, siret, reference).getIri()

        datacoreService.deleteResource(MP_PROJECT, PIECE_TYPE, dcPieceIri, bearer)

    }
}