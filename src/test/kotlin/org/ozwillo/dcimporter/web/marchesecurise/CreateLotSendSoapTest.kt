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
        dce = "1530514543c6yt3jacnk6x"
        libelle = if("Un second test".length > 255) "Un second test".substring(0,255) else "Un second test"
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
        val response = SendSoap.sendSoap("https://www.marches-securises.fr/webserv/?module=dce|serveur_lot_dce",soapMessage)
        println(response)
    }
}