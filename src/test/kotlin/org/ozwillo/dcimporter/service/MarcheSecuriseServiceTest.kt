package org.ozwillo.dcimporter.service

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.equalTo
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.client.postForEntity
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class MarcheSecuriseServiceTest{

    @Value("\${marchesecurise.url.createConsultation}")
    private val createConsultationUrl = ""
    @Value("\${marchesecurise.url.updateConsultation}")
    private val updateConsultationUrl = ""
    @Value("\${marchesecurise.url.deleteConsultation}")
    private val deleteConsultationUrl = ""
    @Value("\${marchesecurise.url.publishConsultation}")
    private val publishConsultationUrl = ""
    @Value("\${marchesecurise.url.lot}")
    private val lotUrl = ""
    @Value("\${marchesecurise.url.piece}")
    private val pieceUrl = ""

    @Value("\${marchesecurise.login}")
    private val login = ""
    @Value("\${marchesecurise.password}")
    private val password = ""
    @Value("\${marchesecurise.pa}")
    private val pa = ""

    private lateinit var wireMockServer:WireMockServer

    private val tokenInfoResponse = """
        {
            "active": "true"
        }
        """

    @Autowired
    private lateinit var restTemplate: TestRestTemplate

    @BeforeAll
    fun setUp(){
        wireMockServer = WireMockServer(WireMockConfiguration.wireMockConfig().port(8990))
        wireMockServer.start()

        WireMock.configureFor(8990)
        WireMock.stubFor(WireMock.post(WireMock.urlMatching("http://localhost:8990"))
                .willReturn(WireMock.okJson(tokenInfoResponse).withStatus(200)))
    }

    @AfterAll
    fun end(){
        wireMockServer.stop()
    }

    @Test
    fun correct_dce_parsing_from_correct_consultation_creation_soap_response(){

        val request = "<soapenv:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:web=\"https://www.marches-securises.fr/webserv/\">\n" +
                "   <soapenv:Header/>\n" +
                "   <soapenv:Body>\n" +
                "      <web:creer_consultation_log soapenv:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n" +
                "         <login xsi:type=\"xsd:string\">$login</login>\n" +
                "         <password xsi:type=\"xsd:string\">$password</password>\n" +
                "         <pa xsi:type=\"xsd:string\">$pa</pa>\n" +
                "      </web:creer_consultation_log>\n" +
                "   </soapenv:Body>\n" +
                "</soapenv:Envelope>"

        wireMockServer.stubFor(WireMock.post(WireMock.urlEqualTo("/webserv/?module=dce%7Cserveur_crea_dce"))
                .withHeader("Content-Type", equalTo("text/plain;charset=UTF-8"))
                .willReturn(aResponse().withBody("<SOAP-ENV:Envelope SOAP-ENV:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\" xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ns1=\"https://www.marches-securises.fr/webserv/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:SOAP-ENC=\"http://schemas.xmlsoap.org/soap/encoding/\">\n" +
                        "   <SOAP-ENV:Body>\n" +
                        "      <ns1:creer_consultation_logResponse>\n" +
                        "         <return xsi:type=\"xsd:string\"><![CDATA[<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<ifw:data xmlns:ifw=\"interbat/framwork-exportation\">\n" +
                        "  <objet type=\"ms_v2__fullweb_dce\">\n" +
                        "    <propriete nom=\"cle\" statut=\"changed\">1533564104hgo8i4u1mjlh</propriete>\n" +
                        "    <propriete nom=\"cle_pa\" statut=\"changed\">1267898337p8xft</propriete>\n" +
                        "    <propriete nom=\"reference\">F-SICTIAM_06_20180806W2_04</propriete>\n" +
                        "    <propriete nom=\"objet\" statut=\"changed\">Consultation Webservice</propriete>\n" +
                        "    <propriete nom=\"date_publication\" statut=\"changed\">1533564104</propriete>\n" +
                        "    <propriete nom=\"date_publication_f\" statut=\"changed\">lundi 06 août 2018 - 16:01</propriete>\n" +
                        "    <propriete nom=\"date_cloture\" statut=\"changed\">1533636000</propriete>\n" +
                        "    <propriete nom=\"date_cloture_f\" statut=\"changed\">mardi 07 août 2018 - 12:00</propriete>\n" +
                        "    <propriete nom=\"ref_interne\" statut=\"changed\">webserv</propriete>\n" +
                        "    <propriete nom=\"finalite_marche\" statut=\"changed\">marche</propriete>\n" +
                        "    <propriete nom=\"type_marche\" statut=\"changed\">public</propriete>\n" +
                        "    <propriete nom=\"type_prestation\" statut=\"changed\">autres</propriete>\n" +
                        "    <propriete nom=\"departements_prestation\"/>\n" +
                        "    <propriete nom=\"passation\"/>\n" +
                        "    <propriete nom=\"informatique\"/>\n" +
                        "    <propriete nom=\"passe\" statut=\"changed\">64VF4F</propriete>\n" +
                        "    <propriete nom=\"emails\"/>\n" +
                        "    <propriete nom=\"en_ligne\" statut=\"changed\">1</propriete>\n" +
                        "    <propriete nom=\"a_lots\" statut=\"changed\">0</propriete>\n" +
                        "    <propriete nom=\"invisible\"/>\n" +
                        "  <nombre_lots nom=\"nb_lot\">0</nombre_lots></objet>\n" +
                        "</ifw:data>]]></return>\n" +
                        "      </ns1:creer_consultation_logResponse>\n" +
                        "   </SOAP-ENV:Body>\n" +
                        "</SOAP-ENV:Envelope>").withStatus(200)).withHeader("Content-Type", equalTo("text/plain;charset=UTF-8")))

        val entity = restTemplate.postForEntity<String>(createConsultationUrl, request)
        Assertions.assertThat(entity.statusCode).isEqualTo(HttpStatus.OK)
        Assertions.assertThat(entity.body).contains("propriete nom=\"cle\" statut=\"changed\"")
        val parsedDce = (entity.body)!!.split("<propriete nom=\"cle\" statut=\"changed\">|</propriete>".toRegex())[1]
        Assertions.assertThat(parsedDce).isEqualTo("1533564104hgo8i4u1mjlh")

        WireMock.verify(WireMock.postRequestedFor(WireMock.urlEqualTo("/webserv/?module=dce%7Cserveur_crea_dce")))
    }

    @Test
    fun dce_parsing_from_incorrect_consultation_creation_soap_response(){
        val request = "package resources.template.templateCreateConsultationLogRequest.groovy\n" +
                "\n" +
                "<soapenv:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:web=\"https://www.marches-securises.fr/webserv/\">\n" +
                "   <soapenv:Header/>\n" +
                "   <soapenv:Body>\n" +
                "      <web:creer_consultation_log soapenv:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n" +
                "         <login xsi:type=\"xsd:int\">wsdev-sictiam</login>\n" +
                "         <password xsi:type=\"xsd:string\">WS*s1ctiam*</password>\n" +
                "         <pa xsi:type=\"xsd:string\">1267898337p8xft</pa>\n" +
                "      </web:creer_consultation_log>\n" +
                "   </soapenv:Body>\n" +
                "</soapenv:Envelope>"

        wireMockServer.stubFor(WireMock.post(WireMock.urlEqualTo("/webserv/?module=dce%7Cserveur_crea_dce"))
                .withHeader("Content-Type", equalTo("text/plain;charset=UTF-8"))
                .willReturn(WireMock.aResponse().withBody("<env:Envelope xmlns:env=\"http://www.w3.org/2003/05/soap-envelope\">\n" +
                        "   <env:Body>\n" +
                        "      <env:Fault>\n" +
                        "         <env:Code>\n" +
                        "            <env:Value>env:Sender</env:Value>\n" +
                        "         </env:Code>\n" +
                        "         <env:Reason>\n" +
                        "            <env:Text>Bad Request</env:Text>\n" +
                        "         </env:Reason>\n" +
                        "      </env:Fault>\n" +
                        "   </env:Body>\n" +
                        "</env:Envelope>").withStatus(500)).withHeader("Content-Type", equalTo("text/plain;charset=UTF-8")))

        val entity = restTemplate.postForEntity<String>(createConsultationUrl, request)
        Assertions.assertThat(entity.statusCode).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
        var parsedDce = ""
        if ((entity.body) != null && (entity.body)!!.contains("<propriete nom=\"cle\" statut=\"changed\">")){
            parsedDce = (entity.body)!!.split("<propriete nom=\"cle\" statut=\"changed\">|</propriete>".toRegex())[1]
        }else{
            parsedDce = "A logger error"
        }
        Assertions.assertThat(parsedDce).isEqualTo("A logger error")

        WireMock.verify(WireMock.postRequestedFor(WireMock.urlEqualTo("/webserv/?module=dce%7Cserveur_crea_dce")))
    }

    @Test
    fun dce_parsing_from_no_response_consultation_creation_soap_response(){

        val request = "<soapenv:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:web=\"https://www.marches-securises.fr/webserv/\">\n" +
                "   <soapenv:Header/>\n" +
                "   <soapenv:Body>\n" +
                "      <web:creer_consultation_log soapenv:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n" +
                "         <login xsi:type=\"xsd:string\">$login</login>\n" +
                "         <password xsi:type=\"xsd:string\">$password</password>\n" +
                "         <pa xsi:type=\"xsd:string\">$pa</pa>\n" +
                "      </web:creer_consultation_log>\n" +
                "   </soapenv:Body>\n" +
                "</soapenv:Envelope>"

        wireMockServer.stubFor(WireMock.post(WireMock.urlEqualTo("/webserv/?module=dce%7Cserveur_crea_dce"))
                .withHeader("Content-Type", equalTo("text/plain;charset=UTF-8"))
                .willReturn(WireMock.aResponse().withFixedDelay(6000).withBody("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
                        "    <SOAP-ENV:Body>\n" +
                        "        <SOAP-ENV:Fault>\n" +
                        "            <faultcode>SOAP-ENV:Server</faultcode>\n" +
                        "            <faultstring>Maximum execution time of 60 seconds exceeded</faultstring>\n" +
                        "        </SOAP-ENV:Fault>\n" +
                        "    </SOAP-ENV:Body>\n" +
                        "</SOAP-ENV:Envelope>").withStatus(500)))

        val entity = restTemplate.postForEntity<String>(createConsultationUrl, request)
        Assertions.assertThat(entity.statusCode).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
        var parsedDce = ""
        if ((entity.body)!!.contains("<propriete nom=\"cle\" statut=\"changed\">")){
            parsedDce = (entity.body)!!.split("<propriete nom=\"cle\" statut=\"changed\">|</propriete>".toRegex())[1]
        }else{
            parsedDce = "A logger error"
        }
        Assertions.assertThat(parsedDce).isEqualTo("A logger error")

        WireMock.verify(WireMock.postRequestedFor(WireMock.urlEqualTo("/webserv/?module=dce%7Cserveur_crea_dce")))

    }

    @Test
    fun cleLot_parsing_from_lot_creation_soap_response(){

    }
}