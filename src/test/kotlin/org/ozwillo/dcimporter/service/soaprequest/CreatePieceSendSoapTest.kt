package org.ozwillo.dcimporter.service.soaprequest

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.ozwillo.dcimporter.util.MSUtils
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.util.*

@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class CreatePieceSendSoapTest{

    @Value("\${marchesecurise.url.piece}")
    private val PIECE_URL = ""

    @Value("\${marchesecurise.login}")
    private var login: String = ""
    @Value("\${marchesecurise.password}")
    private var password: String = ""
    @Value("\${marchesecurise.pa}")
    private var pa: String = ""

    private var dce = ""
    private var cleLot = ""
    private var clePiece = ""
    private var libelle = ""
    private var la = ""
    private var ordre = ""
    private var nom = ""
    private var extension = ""
    private var contenu = ""
    private var poids = ""

    @BeforeAll
    fun setup(){

        val byteArrayContenu = "un contenu texte".toByteArray()

        dce = "15320018624ocnidk7xjqm"
        cleLot = ""
        clePiece = "153200788997bc1fkfmi29"
        libelle = "Test modification pièce3"
        la = MSUtils.booleanToInt(false).toString()
        ordre = 1.toString()
        nom = "NomDuFichierSansTiret6"
        extension = "txt"
        contenu = Base64.getEncoder().encodeToString(byteArrayContenu)
        poids = 10.toString()
    }

    @AfterAll
    fun teardown(){
        dce = ""
        cleLot = ""
        libelle = ""
        la = ""
        ordre = ""
        nom = ""
        extension = ""
        contenu = ""
        poids = ""
    }

    @Test
    fun createPieceTest(){
        val soapMessage = MSUtils.generateCreatePieceLogRequest(login, password, pa, dce, cleLot, libelle, la, ordre, nom, extension, contenu, poids)
        println(soapMessage)
        val response = MSUtils.sendSoap(PIECE_URL, soapMessage)
        println(response)
    }

    @Test
    fun updatePieceTest(){
        val soapMessage = MSUtils.generateModifyPieceLogRequest(login, password, pa, dce, clePiece, cleLot, libelle, ordre, nom, extension, contenu, poids)
        println(soapMessage)
        val response = MSUtils.sendSoap(PIECE_URL, soapMessage)
        println(response)
    }
}