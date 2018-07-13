package org.ozwillo.dcimporter.service.soaprequest

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.ozwillo.dcimporter.util.MSUtils
import org.springframework.beans.factory.annotation.Value

class DeleteConsultationSendSoapTest{

    @Value("\${marchesecurise.config.url.deleteConsultation}")
    private val DELETE_CONSULTATION_URL = ""

    @Value("\${marchesecurise.login}")
    private var login: String = ""
    @Value("\${marchesecurise.password}")
    private var password: String = ""
    @Value("\${marchesecurise.pa}")
    private var pa: String = ""
    private var dce = ""

    @BeforeAll
    fun setup(){
        dce = "1530514543c6yt3jacnk6x"
    }

    @AfterAll
    fun tearDown(){
        dce = ""
    }

    @Test
    fun deleteConsultation(){

        val soapMessage = MSUtils.generateDeleteConsultationLogRequest(login, password, pa, dce)
        println(soapMessage)
        val response = MSUtils.sendSoap(DELETE_CONSULTATION_URL, soapMessage)
        println(response)
    }
}