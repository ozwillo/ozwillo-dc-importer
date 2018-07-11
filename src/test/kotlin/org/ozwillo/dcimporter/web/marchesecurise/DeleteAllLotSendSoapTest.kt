package org.ozwillo.dcimporter.web.marchesecurise

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.ozwillo.dcimporter.model.wsdl.marchesecurise.request.GenerateSoapRequest
import java.util.*

class DeleteAllLotSendSoapTest{
    private var login = ""
    private var password = ""
    private var pa = ""
    private var dce = ""

    @BeforeAll
    fun setUp(){
        login = "wsdev-sictiam"
        password = "WS*s1ctiam*"
        pa = "1267898337p8xft"
        dce = "1530514543c6yt3jacnk6x"
    }

    @AfterAll
    fun tearDown(){
        login = ""
        password = ""
        pa = ""
        dce = ""
    }

    @Test
    fun deleteAllLot(){
        val soapMessage = GenerateSoapRequest.generateDeleteAllLotRequest(login, password, pa, dce)
        println(soapMessage)
        val response = SendSoap.sendSoap(MarcheSecuriseURL.LOTS_URL, soapMessage)
        println(response)
    }
}