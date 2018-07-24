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
class ModifyLotSendSoapTest{

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
    private var libelle = ""
    private var ordre = ""
    private var numero = ""

    @BeforeAll
    fun setup(){
        dce = "15320018624ocnidk7xjqm"
        cleLot = "15320084513j68bf85fmf5"
        libelle = if("Libellé modifié encore une fois".length > 255) "Libellé modifié encore une fois".substring(0,255) else "Libellé modifié encore une fois"
        ordre = 1.toString()
        numero = 1.toString()
    }

    @AfterAll
    fun tearDown(){
        dce = ""
        cleLot = ""
        libelle = ""
        ordre = ""
        numero = ""
    }

    @Test
    fun modifyLot(){
        val soapMessage = MSUtils.generateModifyLotRequest(login, password, pa, dce, cleLot, libelle, ordre, numero)
        println(soapMessage)
        val response = MSUtils.sendSoap(LOT_URL, soapMessage)
        println(response)
    }
}