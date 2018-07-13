package org.ozwillo.dcimporter.service.soaprequest

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.ozwillo.dcimporter.util.MSUtils
import org.springframework.beans.factory.annotation.Value

class ModifyLotSendSoapTest{

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
    private var libelle = ""
    private var ordre = ""
    private var numero = ""

    @BeforeAll
    fun setup(){
        dce = "1530514543c6yt3jacnk6x"
        uuid = "15305146545i8p34km21cr"
        libelle = if("Libellé modifié encore une fois".length > 255) "Libellé modifié encore une fois".substring(0,255) else "Libellé modifié encore une fois"
        ordre = 1.toString()
        numero = 1.toString()
    }

    @AfterAll
    fun tearDown(){
        dce = ""
        uuid = ""
        libelle = ""
        ordre = ""
        numero = ""
    }

    @Test
    fun modifyLot(){
        val soapMessage = MSUtils.generateModifyLotRequest(login, password, pa, dce, uuid, libelle, ordre, numero)
        println(soapMessage)
        val response = MSUtils.sendSoap(LOT_URL, soapMessage)
        println(response)
    }
}