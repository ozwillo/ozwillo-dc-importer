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

@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class DeleteLotSendSoapTest{

    @Value("\${marchesecurise.url.lot}")
    private val LOT_URL = ""


    @Value("\${marchesecurise.login}")
    private var login: String = ""
    @Value("\${marchesecurise.password}")
    private var password: String = ""
    @Value("\${marchesecurise.pa}")
    private var pa: String = ""
    private var dce = ""
    private var cleLot = ""

    @BeforeAll
    fun setup(){
        dce = "1531898869ncvl4stchtz6"
        cleLot = "1531899740ovpc2z7u5utz"
    }

    @AfterAll
    fun tearDown(){
        dce = ""
        cleLot = ""
    }

    @Test
    fun deleteLot(){
        val soapMessage = MSUtils.generateDeleteLotRequest(login, password, pa, dce, cleLot)
        println(soapMessage)
        val response = MSUtils.sendSoap(LOT_URL, soapMessage)
        println(response)
    }
}