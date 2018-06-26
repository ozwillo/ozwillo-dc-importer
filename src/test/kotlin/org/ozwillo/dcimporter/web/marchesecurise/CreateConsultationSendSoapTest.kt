package org.ozwillo.dcimporter.web.marchesecurise

import com.sun.xml.internal.fastinfoset.util.StringArray
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.ozwillo.dcimporter.model.wsdl.marchesecurise.request.CreateConsultationSoapRequest

class CreateConsultationSendSoapTest{

    var login = ""
    var password = ""
    var pa = ""

    @BeforeAll
    fun setup(){
        login = "wsdev-sictiam"
        password = "WS*s1ctiam*"
        pa = "1267898337p8xft"
    }

    @AfterAll
    fun tearDown(){
        login = ""
        password = ""
        pa = ""
    }

    @Test
    fun sendCreateConsultationRequest (){
        val soapMessage = CreateConsultationSoapRequest.generateCreateConsultationLogRequest(login, password,pa)
        print(soapMessage)
        val res:String = CreateConsultation.sendSoap("https://www.marches-securises.fr/webserv/?module=dce|serveur_crea_dce", soapMessage)
        print("$res \n")
        val dce:List<String> = res.split(("&lt;propriete nom=\"cle\" statut=\"changed\"&gt;1530005251drf6k847vi9c&lt;/propriete&gt;").toRegex())



    }
}