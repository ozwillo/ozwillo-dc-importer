package org.ozwillo.dcimporter.web.marchesecurise

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.ozwillo.dcimporter.model.wsdl.marchesecurise.request.GenerateSoapRequest

class ModifyLotSendSoapTest{
    private var login = ""
    private var password = ""
    private var pa = ""
    private var dce = ""
    private var uuid = ""
    private var libelle = ""
    private var ordre = ""
    private var numero = ""

    @BeforeAll
    fun setup(){
        login = "wsdev-sictiam"
        password = "WS*s1ctiam*"
        pa = "1267898337p8xft"
        dce = "15301683035e5q3tg4ve86"
        uuid = "1530176380qfxy3oq5o2fw"
        libelle = if("Libellé modifié encore une fois".length > 255) "Libellé modifié encore une fois".substring(0,255) else "Libellé modifié encore une fois"
        ordre = 2.toString()
        numero = 6.toString()
    }

    @AfterAll
    fun tearDown(){
        login = ""
        password = ""
        pa = ""
        dce = ""
        uuid = ""
        libelle = ""
        ordre = ""
        numero = ""
    }

    @Test
    fun modifyLot(){
        val soapMessage = GenerateSoapRequest.generateModifyLotRequest(login, password, pa, dce, uuid, libelle, ordre, numero)
        println(soapMessage)
        val response = SendSoap.sendSoap("https://www.marches-securises.fr/webserv/?module=dce|serveur_lot_dce", soapMessage)
        println(response)
    }
}