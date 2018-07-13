package org.ozwillo.dcimporter.service.soaprequest

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.ozwillo.dcimporter.util.MSUtils
import org.springframework.beans.factory.annotation.Value

class CreateLotSendSoapTest{

    @Value("\${marchesecurise.config.url.lot}")
    private val LOT_URL = ""


    @Value("\${marchesecurise.login}")
    private var login: String = ""
    @Value("\${marchesecurise.password}")
    private var password: String = ""
    @Value("\${marchesecurise.pa}")
    private var pa: String = ""

    private var dce = ""
    private var libelle = ""
    private var ordre = ""
    private var numero = ""

    @BeforeAll
    fun setup(){
        dce = "1530867066vp68t7r6j484"
        libelle = if("Un premier test".length > 255) "Un premier test".substring(0,255) else "Un premier test"
        ordre = 1.toString()
        numero = 1.toString()
    }

    @AfterAll
    fun tearDown(){
        dce = ""
        libelle = ""
        ordre = ""
        numero = ""
    }

    @Test
    fun createLot(){
        val soapMessage = MSUtils.generateCreateLotLogRequest(login, password, pa, dce, libelle, ordre, numero)
        println(soapMessage)
        val response = MSUtils.sendSoap(LOT_URL,soapMessage)
        println(response)
        val parseResponse = response.split("&lt;propriete nom=\"cle_lot\"&gt;|&lt;/propriete&gt;".toRegex())
        val cleLot = parseResponse[2]
        println(cleLot)
    }
}