package org.ozwillo.dcimporter.web.marchesecurise

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.ozwillo.dcimporter.model.wsdl.marchesecurise.request.GenerateSoapRequest

class CreateLotSendSoapTest{
    private var login = ""
    private var password = ""
    private var pa = ""
    private var dce = ""
    private var libelle = ""
    private var ordre = ""
    private var numero = ""

    @BeforeAll
    fun setup(){
        login = "wsdev-sictiam"
        password = "WS*s1ctiam*"
        pa = "1267898337p8xft"
        dce = "1530867066vp68t7r6j484"
        libelle = if("Un premier test".length > 255) "Un premier test".substring(0,255) else "Un premier test"
        ordre = 1.toString()
        numero = 1.toString()
    }

    @AfterAll
    fun tearDown(){
        login = ""
        password = ""
        pa = ""
        dce = ""
        libelle = ""
        ordre = ""
        numero = ""
    }

    @Test
    fun createLot(){
        val soapMessage = GenerateSoapRequest.generateCreateLotLogRequest(login, password, pa, dce, libelle, ordre, numero)
        println(soapMessage)
        val response = SendSoap.sendSoap(MarcheSecuriseURL.getLotUrl(),soapMessage)
        println(response)
        val parseResponse = response.split("&lt;propriete nom=\"cle_lot\"&gt;|&lt;/propriete&gt;".toRegex())
        val cleLot = parseResponse[2]
        println(cleLot)
    }
}