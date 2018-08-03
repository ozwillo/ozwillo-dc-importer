package org.ozwillo.dcimporter.service.soaprequest

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.ozwillo.dcimporter.model.marchepublic.FinaliteMarcheType
import org.ozwillo.dcimporter.model.marchepublic.TypeMarcheType
import org.ozwillo.dcimporter.model.marchepublic.TypePrestationType
import org.ozwillo.dcimporter.util.MSUtils
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.LocalDateTime
import java.time.ZoneId

@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class CreateConsultationSendSoapTest{

    @Value("\${marchesecurise.url.createConsultation}")
    private val createConsultationUrl = ""
    @Value("\${marchesecurise.url.updateConsultation}")
    private val updateConsultationUrl = ""
    @Value("\${marchesecurise.url.publishConsultation}")
    private val publishConsultationUrl = ""



    @Value("\${marchesecurise.login}")
    private var login: String = ""
    @Value("\${marchesecurise.password}")
    private var password: String = ""
    @Value("\${marchesecurise.pa}")
    private var pa: String = ""


    private var objet = ""
    private var enligne = ""
    private var datePublication = ""
    private var dateCloture = ""
    private var reference = ""
    private var finaliteMarche = ""
    private var typeMarche = ""
    private var prestation = ""
    private var passation = ""
    private var informatique = ""
    private var alloti = ""
    private var departement = ""
    private var email = ""


    @BeforeAll
    fun setup(){

        objet = if("Consultation WS Test".length > 255) "Consultation WS Test".substring(0,255) else "Consultation WS Test"
        enligne = MSUtils.booleanToInt(true).toString()
        datePublication = LocalDateTime.now().atZone(ZoneId.of("Europe/Paris")).toInstant().epochSecond.toString()
        dateCloture = LocalDateTime.now().plusMonths(3).atZone(ZoneId.of("Europe/Paris")).toInstant().epochSecond.toString()
        reference = if("F-SICTIAM_06_20180622W2_01".toString().length > 255) ("F-SICTIAM_06_20180622W2_01".toString()).substring(0,255) else "F-SICTIAM_06_20180622W2_01".toString()
        finaliteMarche = FinaliteMarcheType.AUTRE.toString().toLowerCase()
        typeMarche = TypeMarcheType.AUTRE.toString().toLowerCase()
        prestation = TypePrestationType.AUTRES.toString().toLowerCase()
        passation = "AORA"
        informatique = MSUtils.booleanToInt(true).toString()
        alloti = MSUtils.booleanToInt(false).toString()
        departement = MSUtils.intListToString(listOf(74, 38, 6))
        email = if(MSUtils.stringListToString(listOf("test1@test.com", "test2@test.com", "test3@test.com")).length > 255) (MSUtils.stringListToString(listOf("test1@test.com", "test2@test.com", "test3@test.com"))).substring(0,255) else MSUtils.stringListToString(listOf("test1@test.com", "test2@test.com", "test3@test.com"))
    }

    @AfterAll
    fun tearDown(){

        objet = ""
        enligne = ""
        datePublication = ""
        dateCloture = ""
        reference = ""
        finaliteMarche = ""
        typeMarche = ""
        prestation = ""
        passation = ""
        informatique = ""
        alloti = ""
        departement = ""
        email = ""
    }



    private fun sendCreateConsultationRequest (url:String, login:String, password:String, pa:String):String{
        val soapMessage = MSUtils.generateCreateConsultationLogRequest(login, password,pa)

        return MSUtils.sendSoap(url, soapMessage)
    }

    private fun getDce(url:String, login: String, password: String, pa: String):String {
        val parseResponse: List<String> = sendCreateConsultationRequest(url, login, password, pa).split("<propriete nom=\"cle\" statut=\"changed\">|</propriete>".toRegex())
        return parseResponse[1]
    }

    @Test
    fun sendCreateConsultationRequest (){

        val dce = getDce(createConsultationUrl, login, password, pa)
        println(dce)
        val soapMessage = MSUtils.generateModifyConsultationLogRequest(login, password, pa, dce, objet, enligne, datePublication, dateCloture, reference, finaliteMarche, typeMarche, prestation, passation, informatique, alloti, departement, email)
        println(soapMessage)
        val response = MSUtils.sendSoap(updateConsultationUrl, soapMessage)
        print(response)
    }

    @Test
    fun checkConsultationRequestTest(){
        val dce = "1533130670tfumxby3j9u5"
        val soapMessage = MSUtils.generateCheckConsultationRequest(login, password, pa, dce)
        println(soapMessage)
        val response = MSUtils.sendSoap(publishConsultationUrl, soapMessage)
        if (response.contains("validation_erreur")){
            val error = (response.split("<validation_erreur erreur_0=|cle=".toRegex()))[1]
            println(error)
        }else{
            println(response)
        }
    }

    @Test
    fun publishConsultationTest(){
        val dce = "1533111686xmk56r7ieh8f"
        val soapMessage = MSUtils.generatePublishConsultationRequest(login, password, pa, dce)
        println(soapMessage)
        val response = MSUtils.sendSoap(publishConsultationUrl, soapMessage)
    }

    @Test
    fun badSoapEnvelopeTest(){

        val dce = getDce(createConsultationUrl, login, password, pa)

        val soapMessage = "<soapenv:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:web=\"https://www.marches-securises.fr/webserv/\">\n" +
                "    <soapenv:Header/>\n" +
                "    <soapenv:Body>\n" +
                "        <web:modifier_consultation_log soapenv:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n" +
                "            <login xsi:type=\"xsd:string\">$login</login>\n" +
                "            <password xsi:type=\"xsd:string\">$password</password>\n" +
                "            <pa xsi:type=\"xsd:string\">$pa</pa>\n" +
                "            <dce xsi:type=\"xsd:string\">$dce</dce>\n" +
                "            <tableau xsi:type=\"Map\" xmlns=\"http://xml.apache.org/xml-soap\">\n" +
                "                <item>\n" +
                "                    <key xsi:type=\"xsd:string\">objet</key>\n" +
                "                    <value xsi:type=\"xsd:string\">$objet</value>\n" +
                "                </item>\n" +
                "                <item>\n" +
                "                    <key xsi:type=\"xsd:string\">en_ligne</key>\n" +
                "                    <value xsi:type=\"xsd:int\">$enligne</value>\n" +
                "                </item>\n" +
                "                <item>\n" +
                "                    <key xsi:type=\"xsd:string\">date_publication</key>\n" +
                "                    <value xsi:type=\"xsd:timestamp\">$datePublication</value>\n" +
                "                </item>\n" +
                "                <item>\n" +
                "                    <key xsi:type=\"xsd:string\">date_cloture</key>\n" +
                "                    <value xsi:type=\"xsd:timestamp\">$dateCloture</value>\n" +
                "                </item>\n" +
                "                <item>\n" +
                "                    <key xsi:type=\"xsd:string\">ref_interne</key>\n" +
                "                    <value xsi:type=\"xsd:string\">$reference</value>\n" +
                "                </item>\n" +
                "                <item>\n" +
                "                    <key xsi:type=\"xsd:string\">finalite_marche</key>\n" +
                "                    <value xsi:type=\"xsd:string\">$finaliteMarche</value>\n" +
                "                </item>\n" +
                "                <item>\n" +
                "                    <key xsi:type=\"xsd:string\">type_marche</key>\n" +
                "                    <value xsi:type=\"xsd:string\">$typeMarche</value>\n" +
                "                </item>\n" +
                "                <item>\n" +
                "                    <key xsi:type=\"xsd:string\">type_prestation</key>\n" +
                "                    <value xsi:type=\"xsd:string\">$prestation</value>\n" +
                "                </item>\n" +
                "                <item>\n" +
                "                    <key xsi:type=\"xsd:string\">passation</key>\n" +
                "                    <value xsi:type=\"xsd:string\">$passation</value>\n" +
                "                </item>\n" +
                "                <item>\n" +
                "                    <key xsi:type=\"xsd:string\">a_lots</key>\n" +
                "                    <value xsi:type=\"xsd:int\">$alloti</value>\n" +
                "                </item>\n" +
                "                <item>\n" +
                "                    <key xsi:type=\"xsd:string\">departements_prestation</key>\n" +
                "                    <value xsi:type=\"xsd:string\">$departement</value>\n" +
                "                </item>\n" +
                "                <item>\n" +
                "                    <key xsi:type=\"xsd:string\">emails</key>\n" +
                "                    <value xsi:type=\"xsd:int\">$email</value>\n" +
                "                </item>\n" +
                "            </tableau>     \n" +
                "        </web:modifier_consultation_log>\n" +
                "    </soapenv:Body>\n" +
                "</soapenv:Envelope>"

        val response = MSUtils.sendSoap(updateConsultationUrl, soapMessage)
        println(response)
    }
}