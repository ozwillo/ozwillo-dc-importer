package org.ozwillo.dcimporter.service.soaprequest

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.ozwillo.dcimporter.util.MSUtils
import org.springframework.beans.factory.annotation.Value

class DeleteAllLotSendSoapTest{

    @Value("\${marchesecurise.config.url.lot}")
    private val LOT_URL = ""


    @Value("\${marchesecurise.login}")
    private var login: String = ""
    @Value("\${marchesecurise.password}")
    private var password: String = ""
    @Value("\${marchesecurise.pa}")
    private var pa: String = ""

    private var dce = ""

    @BeforeAll
    fun setUp(){

        dce = "1530514543c6yt3jacnk6x"
    }

    @AfterAll
    fun tearDown(){
        dce = ""
    }

    @Test
    fun deleteAllLot(){
        val soapMessage = MSUtils.generateDeleteAllLotRequest(login, password, pa, dce)
        println(soapMessage)
        val response = MSUtils.sendSoap(LOT_URL, soapMessage)
        println(response)
    }
}