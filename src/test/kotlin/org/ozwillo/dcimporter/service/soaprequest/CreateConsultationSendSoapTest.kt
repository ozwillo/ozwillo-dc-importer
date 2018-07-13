package org.ozwillo.dcimporter.service.soaprequest

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.ozwillo.dcimporter.model.marchepublic.FinaliteMarcheType
import org.ozwillo.dcimporter.model.marchepublic.TypeMarcheType
import org.ozwillo.dcimporter.model.marchepublic.TypePrestationType
import org.ozwillo.dcimporter.util.DCUtils
import org.ozwillo.dcimporter.util.MSUtils
import org.springframework.beans.factory.annotation.Value
import java.sql.Timestamp
import java.time.LocalDateTime
import java.time.Month

class CreateConsultationSendSoapTest{

    @Value("\${marchesecurise.config.url.createConsultation}")
    private val CREATE_CONSULTATION_URL = ""
    @Value("\${marchesecurise.config.url.updateConsultation}")
    private val UPDATE_CONSULTATION_URL = ""


    @Value("\${marchesecurise.login}")
    private var login: String = ""
    @Value("\${marchesecurise.password}")
    private var password: String = ""
    @Value("\${marchesecurise.pa}")
    private var pa: String = ""


    private var objet = ""
    private var enligne = ""
    private var datePublication = ""
    private var dateCloture = ""
    private var reference = ""
    private var finaliteMarche = ""
    private var typeMarche = ""
    private var prestation = ""
    private var passation = ""
    private var alloti = ""
    private var departement = ""
    private var email = ""


    @BeforeAll
    fun setup(){

        objet = if("Consultation WS Test".length > 255) "Consultation WS Test".substring(0,255) else "Consultation WS Test"
        enligne = DCUtils.booleanToInt(true).toString()
        datePublication = ((Timestamp.valueOf(LocalDateTime.now()).time)/1000).toString()
        dateCloture = ((Timestamp.valueOf(LocalDateTime.of(2018,Month.JULY,19,3,0,0,0)).time)/1000).toString()
        reference = if("F-SICTIAM_06_20180622W2_01".toString().length > 255) ("F-SICTIAM_06_20180622W2_01".toString()).substring(0,255) else "F-SICTIAM_06_20180622W2_01".toString()
        finaliteMarche = FinaliteMarcheType.AUTRE.toString().toLowerCase()
        typeMarche = TypeMarcheType.AUTRE.toString().toLowerCase()
        prestation = TypePrestationType.AUTRES.toString().toLowerCase()
        passation = "AORA"
        alloti = DCUtils.booleanToInt(false).toString()
        departement = DCUtils.intListToString(listOf(74, 38, 6))
        email = if(DCUtils.stringListToString(listOf("test1@test.com", "test2@test.com", "test3@test.com")).length > 255) (DCUtils.stringListToString(listOf("test1@test.com", "test2@test.com", "test3@test.com"))).substring(0,255) else DCUtils.stringListToString(listOf("test1@test.com", "test2@test.com", "test3@test.com"))
    }

    @AfterAll
    fun tearDown(){

        objet = ""
        enligne = ""
        datePublication = ""
        dateCloture = ""
        reference = ""
        finaliteMarche = ""
        typeMarche = ""
        prestation = ""
        passation = ""
        alloti = ""
        departement = ""
        email = ""
    }



    private fun sendCreateConsultationRequest (url:String, login:String, password:String, pa:String):String{
        val soapMessage = MSUtils.generateCreateConsultationLogRequest(login, password,pa)

        return MSUtils.sendSoap(url, soapMessage)
    }

    private fun getDce(url:String, login: String, password: String, pa: String):String {
        val parseResponse: List<String> = sendCreateConsultationRequest(url, login, password, pa).split("&lt;propriete nom=\"cle\" statut=\"changed\"&gt;|&lt;/propriete&gt;".toRegex())
        return parseResponse[1]
    }

    @Test
    fun sendCreateConsultationRequest (){

        val dce = getDce(CREATE_CONSULTATION_URL, login, password, pa)
        println(dce)
        val soapMessage = MSUtils.generateModifyConsultationLogRequest(login, password, pa, dce, objet, enligne, datePublication, dateCloture, reference, finaliteMarche, typeMarche, prestation, passation, alloti, departement, email)
        println(soapMessage)
        val response = MSUtils.sendSoap(UPDATE_CONSULTATION_URL, soapMessage)
        print(response)
    }
}