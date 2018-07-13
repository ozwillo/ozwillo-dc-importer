package org.ozwillo.dcimporter.service.soaprequest

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.ozwillo.dcimporter.util.MSUtils
import org.springframework.beans.factory.annotation.Value

class DeleteLotSendSoapTest{

    @Value("\${marchesecurise.config.url.lot}")
    private val LOT_URL = ""


    @Value("\${marchesecurise.login}")
    private var login: String = ""
    @Value("\${marchesecurise.password}")
    private var password: String = ""
    @Value("\${marchesecurise.pa}")
    private var pa: String = ""
    private var dce = ""
    private var uuid = ""

    @BeforeAll
    fun setup(){
        dce = "1530514543c6yt3jacnk6x"
        uuid = "15305146545i8p34km21cr"
    }

    @AfterAll
    fun tearDown(){
        dce = ""
        uuid = ""
    }

    @Test
    fun deleteLot(){
        val soapMessage = MSUtils.generateDeleteLotRequest(login, password, pa, dce, uuid)
        println(soapMessage)
        val response = MSUtils.sendSoap(LOT_URL, soapMessage)
        println(response)
    }
}