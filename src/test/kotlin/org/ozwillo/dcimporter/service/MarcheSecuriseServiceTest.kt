package org.ozwillo.dcimporter.service

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import org.apache.http.impl.conn.Wire
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.ozwillo.dcimporter.model.BusinessMapping
import org.ozwillo.dcimporter.model.marchepublic.*
import org.ozwillo.dcimporter.repository.BusinessAppConfigurationRepository
import org.ozwillo.dcimporter.repository.BusinessMappingRepository
import org.ozwillo.dcimporter.util.MSUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.client.postForEntity
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

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

    private val siret = "123456789"

    private val dce = "1533297690p44lmzk2fidz"
    private val objet = if("Consultation WS Test".length > 255) "Consultation WS Test".substring(0,255) else "Consultation WS Test"
    private val enligne = MSUtils.booleanToInt(true).toString()
    private val datePublication = LocalDateTime.now().atZone(ZoneId.of("Europe/Paris")).toInstant().epochSecond.toString()
    private val dateCloture = LocalDateTime.now().plusMonths(3).atZone(ZoneId.of("Europe/Paris")).toInstant().epochSecond.toString()
    private val reference = if("F-SICTIAM_06_20180622W2_01".toString().length > 255) ("F-SICTIAM_06_20180622W2_01".toString()).substring(0,255) else "F-SICTIAM_06_20180622W2_01".toString()
    private val finaliteMarche = FinaliteMarcheType.AUTRE.toString().toLowerCase()
    private val typeMarche = TypeMarcheType.AUTRE.toString().toLowerCase()
    private val prestation = TypePrestationType.AUTRES.toString().toLowerCase()
    private val alloti = MSUtils.booleanToInt(false).toString()

    private val cleLot = "1532963100xz12dzos6jyh"
    private val libelleLot = if("Un premier test".length > 255) "Un premier test".substring(0,255) else "Un premier test"
    private val ordreLot = 1.toString()
    private val ordreIntLot = 1
    private val numeroLot = 1.toString()

    private val clePiece = "1532504326yqgjft2lti7x"
    private val libellePiece = "Une pièce"
    private val la = MSUtils.booleanToInt(false).toString()
    private val ordrePiece = 1.toString()
    private val ordreIntPiece = 1
    private val nom = "NomDuFichierSansTiret6"
    private val extension = "txt"
    private val byteArrayContenu = "un contenu texte".toByteArray()
    private val contenu = Base64.getEncoder().encodeToString(byteArrayContenu)
    private val poids = 10.toString()

    private lateinit var wireMockServer:WireMockServer

    private val tokenInfoResponse = """
        {
            "active": "true"
        }
        """

    @Autowired
    private lateinit var restTemplate: TestRestTemplate

    @MockBean
    private lateinit var marcheSecuriseService: MarcheSecuriseService
    @Mock
    private lateinit var businessMappingRepository: BusinessMappingRepository
    @Mock
    private lateinit var businessAppConfigurationRepository: BusinessAppConfigurationRepository


    @BeforeAll
    fun setUp(){
        marcheSecuriseService = MarcheSecuriseService(businessMappingRepository, businessAppConfigurationRepository)
        wireMockServer = WireMockServer(WireMockConfiguration.wireMockConfig().port(8990))
        wireMockServer.start()

        WireMock.configureFor(8990)
        WireMock.stubFor(WireMock.post(WireMock.urlEqualTo("http://localhost:8990"))
                .willReturn(WireMock.aResponse().withStatus(200)))
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
                .withRequestBody(equalToXml(request))
                .willReturn(aResponse().withBody("<SOAP-ENV:Envelope SOAP-ENV:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\" xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ns1=\"https://www.marches-securises.fr/webserv/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:SOAP-ENC=\"http://schemas.xmlsoap.org/soap/encoding/\">\n" +
                        "   <SOAP-ENV:Body>\n" +
                        "      <ns1:creer_consultation_logResponse>\n" +
                        "         <return xsi:type=\"xsd:string\"><![CDATA[<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<ifw:data xmlns:ifw=\"interbat/framwork-exportation\">\n" +
                        "  <objet type=\"ms_v2__fullweb_dce\">\n" +
                        "    <propriete nom=\"cle\" statut=\"changed\">$dce</propriete>\n" +
                        "    <propriete nom=\"cle_pa\" statut=\"changed\">$pa</propriete>\n" +
                        "    <propriete nom=\"reference\">F-SICTIAM_06_20180806W2_04</propriete>\n" +
                        "    <propriete nom=\"objet\" statut=\"changed\">$objet</propriete>\n" +
                        "    <propriete nom=\"date_publication\" statut=\"changed\">$datePublication</propriete>\n" +
                        "    <propriete nom=\"date_publication_f\" statut=\"changed\">lundi 06 août 2018 - 16:01</propriete>\n" +
                        "    <propriete nom=\"date_cloture\" statut=\"changed\">$dateCloture</propriete>\n" +
                        "    <propriete nom=\"date_cloture_f\" statut=\"changed\">mardi 07 août 2018 - 12:00</propriete>\n" +
                        "    <propriete nom=\"ref_interne\" statut=\"changed\">$reference</propriete>\n" +
                        "    <propriete nom=\"finalite_marche\" statut=\"changed\">$finaliteMarche</propriete>\n" +
                        "    <propriete nom=\"type_marche\" statut=\"changed\">$typeMarche</propriete>\n" +
                        "    <propriete nom=\"type_prestation\" statut=\"changed\">$prestation</propriete>\n" +
                        "    <propriete nom=\"departements_prestation\"/>\n" +
                        "    <propriete nom=\"passation\"/>\n" +
                        "    <propriete nom=\"informatique\"/>\n" +
                        "    <propriete nom=\"passe\" statut=\"changed\">passe</propriete>\n" +
                        "    <propriete nom=\"emails\"/>\n" +
                        "    <propriete nom=\"en_ligne\" statut=\"changed\">$enligne</propriete>\n" +
                        "    <propriete nom=\"a_lots\" statut=\"changed\">$alloti</propriete>\n" +
                        "    <propriete nom=\"invisible\"/>\n" +
                        "  <nombre_lots nom=\"nb_lot\">0</nombre_lots></objet>\n" +
                        "</ifw:data>]]></return>\n" +
                        "      </ns1:creer_consultation_logResponse>\n" +
                        "   </SOAP-ENV:Body>\n" +
                        "</SOAP-ENV:Envelope>").withStatus(200)).withHeader("Content-Type", equalTo("text/plain;charset=UTF-8")))

        val entity = restTemplate.postForEntity<String>(createConsultationUrl, request)
        Assertions.assertThat(entity.statusCode).isEqualTo(HttpStatus.OK)
        Assertions.assertThat(entity.body).contains("propriete nom=\"cle\" statut=\"changed\"")
        val parsedDce = marcheSecuriseService.parseDceFromResponse(entity.body.toString())
        Assertions.assertThat(parsedDce).isEqualTo(dce)

        WireMock.verify(WireMock.postRequestedFor(WireMock.urlEqualTo("/webserv/?module=dce%7Cserveur_crea_dce")))
    }

    @Test
    fun dce_parsing_from_incorrect_consultation_creation_soap_response(){
        val request = "<soapenv:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:web=\"https://www.marches-securises.fr/webserv/\">\n" +
                "   <soapenv:Header/>\n" +
                "   <soapenv:Body>\n" +
                "      <web:creer_consultation_log soapenv:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n" +
                "         <login xsi:type=\"xsd:int\">$login</login>\n" +
                "         <password xsi:type=\"xsd:string\">$password</password>\n" +
                "         <pa xsi:type=\"xsd:string\">$pa</pa>\n" +
                "      </web:creer_consultation_log>\n" +
                "   </soapenv:Body>\n" +
                "</soapenv:Envelope>"

        wireMockServer.stubFor(WireMock.post(WireMock.urlEqualTo("/webserv/?module=dce%7Cserveur_crea_dce"))
                .withHeader("Content-Type", equalTo("text/plain;charset=UTF-8"))
                .withRequestBody(equalToXml(request))
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
        val parsedDce = marcheSecuriseService.parseDceFromResponse(entity.body.toString())
        Assertions.assertThat(parsedDce).isEmpty()

        WireMock.verify(WireMock.postRequestedFor(WireMock.urlEqualTo("/webserv/?module=dce%7Cserveur_crea_dce")))
    }

    @Test
    fun dce_parsing_from_no_response_from_consultation_creation_soap_request(){

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
                .withRequestBody(equalToXml(request))
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
        var parsedDce = marcheSecuriseService.parseDceFromResponse(entity.body.toString())
        Assertions.assertThat(parsedDce).isEmpty()

        WireMock.verify(WireMock.postRequestedFor(WireMock.urlEqualTo("/webserv/?module=dce%7Cserveur_crea_dce")))

    }

    @Test
    fun correct_cleLot_parsing_from_lot_creation_soap_response(){
        val request = "<soapenv:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:web=\"https://www.marches-securises.fr/webserv/\">\n" +
                "    <soapenv:Header/>\n" +
                "    <soapenv:Body>\n" +
                "        <web:creer_lot_consultation_log soapenv:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n" +
                "            <login xsi:type=\"xsd:string\">$login</login>\n" +
                "            <password xsi:type=\"xsd:string\">$password</password>\n" +
                "            <pa xsi:type=\"xsd:string\">$pa</pa>\n" +
                "            <dce xsi:type=\"xsd:string\">$dce</dce>\n" +
                "            <libelle xsi:type=\"xsd:string\">$libelleLot</libelle>\n" +
                "            <ordre xsi:type=\"xsd:string\">$ordreLot</ordre>\n" +
                "            <numero xsi:type=\"xsd:string\">$numeroLot</numero>\n" +
                "        </web:creer_lot_consultation_log>\n" +
                "    </soapenv:Body>\n" +
                "</soapenv:Envelope>"

        wireMockServer.stubFor(WireMock.post(WireMock.urlEqualTo("/webserv/?module=dce%7Cserveur_lot_dce"))
                .withHeader("Content-Type", equalTo("text/plain;charset=UTF-8"))
                .withRequestBody(equalToXml(request))
                .willReturn(WireMock.aResponse().withBody("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ns1=\"https://www.marches-securises.fr/webserv/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:SOAP-ENC=\"http://schemas.xmlsoap.org/soap/encoding/\" SOAP-ENV:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n" +
                        "    <SOAP-ENV:Body>\n" +
                        "        <ns1:creer_lot_consultation_logResponse>\n" +
                        "            <return xsi:type=\"xsd:string\"><?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<ifw:data xmlns:ifw=\"interbat/framwork-exportation\">\n" +
                        "  <objet type=\"ms_v2__fullweb_lot\">\n" +
                        "    <propriete nom=\"cle\">$cleLot</propriete>\n" +
                        "    <propriete nom=\"cle_lot\">$cleLot</propriete>\n" +
                        "    <propriete nom=\"cle_dce\">$dce</propriete>\n" +
                        "    <propriete nom=\"numero\">$numeroLot</propriete>\n" +
                        "    <propriete nom=\"libelle\">$libelleLot</propriete>\n" +
                        "    <propriete nom=\"ordre\">$ordreLot</propriete>\n" +
                        "  <nombre_lots nom=\"nb_lot\">3</nombre_lots></objet>\n" +
                        "  <objet type=\"ms_v2__fullweb_lot\">\n" +
                        "    <propriete nom=\"cle\">1533627408ru9qybjiinzd</propriete>\n" +
                        "    <propriete nom=\"cle_lot\">1533627408ru9qybjiinzd</propriete>\n" +
                        "    <propriete nom=\"cle_dce\">$dce</propriete>\n" +
                        "    <propriete nom=\"numero\">2</propriete>\n" +
                        "    <propriete nom=\"libelle\">Un second test</propriete>\n" +
                        "    <propriete nom=\"ordre\">2</propriete>\n" +
                        "  <nombre_lots nom=\"nb_lot\">3</nombre_lots></objet>\n" +
                        "  <objet type=\"ms_v2__fullweb_lot\">\n" +
                        "    <propriete nom=\"cle\">153362742719tao4vsd66c</propriete>\n" +
                        "    <propriete nom=\"cle_lot\">153362742719tao4vsd66c</propriete>\n" +
                        "    <propriete nom=\"cle_dce\">$dce</propriete>\n" +
                        "    <propriete nom=\"numero\">3</propriete>\n" +
                        "    <propriete nom=\"libelle\">Un troisième test</propriete>\n" +
                        "    <propriete nom=\"ordre\">3</propriete>\n" +
                        "  <nombre_lots nom=\"nb_lot\">3</nombre_lots></objet>\n" +
                        "</ifw:data>\n" +
                        "</return>\n" +
                        "        </ns1:creer_lot_consultation_logResponse>\n" +
                        "    </SOAP-ENV:Body>\n" +
                        "</SOAP-ENV:Envelope>").withStatus(200)))

        val entity = restTemplate.postForEntity<String>(lotUrl, request)
        Assertions.assertThat(entity.statusCode).isEqualTo(HttpStatus.OK)

        val lot = Lot(uuid = UUID.randomUUID().toString(), libelle = libelleLot, ordre = ordreIntLot, numero = 1)
        val parsedCleLot = marcheSecuriseService.parseCleLot(entity.body.toString(),lot)

        Assertions.assertThat(parsedCleLot).isEqualTo(cleLot)
        WireMock.verify(WireMock.postRequestedFor(WireMock.urlEqualTo("/webserv/?module=dce%7Cserveur_lot_dce")))
    }

    @Test
    fun cleLot_parsing_from_incorrect_lot_creation_response(){
        val request = "<soapenv:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:web=\"https://www.marches-securises.fr/webserv/\">\n" +
                "    <soapenv:Header/>\n" +
                "    <soapenv:Body>\n" +
                "        <web:creer_lot_consultation_log soapenv:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n" +
                "            <login xsi:type=\"xsd:string\">$login</login>\n" +
                "            <password xsi:type=\"xsd:string\">$password</password>\n" +
                "            <pa xsi:type=\"xsd:string\">$pa</pa>\n" +
                "            <dce xsi:type=\"xsd:string\">aWrongDce</dce>\n" +
                "            <libelle xsi:type=\"xsd:string\">$libelleLot</libelle>\n" +
                "            <ordre xsi:type=\"xsd:string\">$ordreLot</ordre>\n" +
                "            <numero xsi:type=\"xsd:string\">$numeroLot</numero>\n" +
                "        </web:creer_lot_consultation_log>\n" +
                "    </soapenv:Body>\n" +
                "</soapenv:Envelope>"

        wireMockServer.stubFor(post(WireMock.urlEqualTo("/webserv/?module=dce%7Cserveur_lot_dce"))
                .withHeader("Content-Type", equalTo("text/plain;charset=UTF-8"))
                .withRequestBody(equalToXml(request))
                .willReturn(WireMock.aResponse().withBody("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ns1=\"https://www.marches-securises.fr/webserv/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:SOAP-ENC=\"http://schemas.xmlsoap.org/soap/encoding/\" SOAP-ENV:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n" +
                        "    <SOAP-ENV:Body>\n" +
                        "        <ns1:creer_lot_consultation_logResponse>\n" +
                        "            <return xsi:type=\"xsd:string\"><?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<ifw:data xmlns:ifw=\"interbat/framwork-exportation\">\n" +
                        "  <objet type=\"error\">\n" +
                        "    <propriete nom=\"load_dce_error\">error</propriete>\n" +
                        "  </objet>\n" +
                        "</ifw:data>\n" +
                        "</return>\n" +
                        "        </ns1:creer_lot_consultation_logResponse>\n" +
                        "    </SOAP-ENV:Body>\n" +
                        "</SOAP-ENV:Envelope>").withStatus(200)))

        val entity = restTemplate.postForEntity<String>(lotUrl, request)
        Assertions.assertThat(entity.statusCode).isEqualTo(HttpStatus.OK)

        val lot = Lot(uuid = UUID.randomUUID().toString(), libelle = libelleLot, ordre = ordreIntLot, numero = 1)
        val parsedCleLot = marcheSecuriseService.parseCleLot(entity.body.toString(),lot)

        Assertions.assertThat(parsedCleLot).isEmpty()
        WireMock.verify(WireMock.postRequestedFor(WireMock.urlEqualTo("/webserv/?module=dce%7Cserveur_lot_dce")))

    }

    @Test
    fun parse_cleLot_from_no_response_from_lot_creation_soap_request(){
        val request = "<soapenv:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:web=\"https://www.marches-securises.fr/webserv/\">\n" +
                "    <soapenv:Header/>\n" +
                "    <soapenv:Body>\n" +
                "        <web:creer_lot_consultation_log soapenv:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n" +
                "            <login xsi:type=\"xsd:string\">$login</login>\n" +
                "            <password xsi:type=\"xsd:string\">$password</password>\n" +
                "            <pa xsi:type=\"xsd:string\">$pa</pa>\n" +
                "            <dce xsi:type=\"xsd:string\">$dce</dce>\n" +
                "            <libelle xsi:type=\"xsd:string\">$libelleLot</libelle>\n" +
                "            <ordre xsi:type=\"xsd:string\">$ordreLot</ordre>\n" +
                "            <numero xsi:type=\"xsd:string\">$numeroLot</numero>\n" +
                "        </web:creer_lot_consultation_log>\n" +
                "    </soapenv:Body>\n" +
                "</soapenv:Envelope>"

        wireMockServer.stubFor(post(WireMock.urlEqualTo("/webserv/?module=dce%7Cserveur_lot_dce"))
                .withHeader("Content-Type", equalTo("text/plain;charset=UTF-8"))
                .withRequestBody(equalToXml(request))
                .willReturn(WireMock.aResponse().withFixedDelay(6000).withBody("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        " <SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
                        "     <SOAP-ENV:Body>\n" +
                        "        <SOAP-ENV:Fault>\n" +
                        "            <faultcode>SOAP-ENV:Server</faultcode>\n" +
                        "            <faultstring>Maximum execution time of 60 seconds exceeded</faultstring>\n" +
                        "        </SOAP-ENV:Fault>\n" +
                        "    </SOAP-ENV:Body>\n" +
                        "</SOAP-ENV:Envelope>").withStatus(500)))

        val entity = restTemplate.postForEntity<String>(lotUrl, request)
        Assertions.assertThat(entity.statusCode).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)

        val lot = Lot(uuid = UUID.randomUUID().toString(), libelle = libelleLot, ordre = ordreIntLot, numero = 1)
        val parsedCleLot = marcheSecuriseService.parseCleLot(entity.body.toString(), lot)

        Assertions.assertThat(parsedCleLot).isEmpty()
        WireMock.verify(WireMock.postRequestedFor(WireMock.urlEqualTo("/webserv/?module=dce%7Cserveur_lot_dce")))
    }

    @Test
    fun correct_clePiece_parsing_from_piece_creation_soap_response(){
        val request = "<soapenv:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:web=\"https://www.marches-securises.fr/webserv/\" xmlns:s2=\"http://xml.apache.org/xml-soap\">\n" +
                "   <soapenv:Header/>\n" +
                "   <soapenv:Body>\n" +
                "      <web:nouveau_fichier_log soapenv:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n" +
                "         <login xsi:type=\"xsd:string\">$login</login>\n" +
                "         <mdp xsi:type=\"xsd:string\">$password</mdp>\n" +
                "         <pa xsi:type=\"xsd:string\">$pa</pa>\n" +
                "         <dce xsi:type=\"xsd:string\">$dce</dce>\n" +
                "         <fichier xsi:type=\"s2:Map\">\n" +
                "          <item>\n" +
                "            <key xsi:type=\"xsd:string\">lot</key>\n" +
                "            <value xsi:type=\"xsd:string\"></value>\n" +
                "          </item>\n" +
                "          <item>\n" +
                "            <key xsi:type=\"xsd:string\">libelle</key>\n" +
                "            <value xsi:type=\"xsd:string\">$libellePiece</value>\n" +
                "          </item>\n" +
                "          <item>\n" +
                "            <key xsi:type=\"xsd:string\">la</key>\n" +
                "            <value xsi:type=\"xsd:string\">$la</value>\n" +
                "          </item>\n" +
                "          <item>\n" +
                "            <key xsi:type=\"xsd:string\">ordre</key>\n" +
                "            <value xsi:type=\"xsd:int\">$ordrePiece</value>\n" +
                "          </item>\n" +
                "          <item>\n" +
                "            <key xsi:type=\"xsd:string\">nom</key>\n" +
                "            <value xsi:type=\"xsd:string\">$nom</value>\n" +
                "          </item>\n" +
                "          <item>\n" +
                "            <key xsi:type=\"xsd:string\">extension</key>\n" +
                "            <value xsi:type=\"xsd:string\">$extension</value>\n" +
                "          </item>\n" +
                "          <item>\n" +
                "            <key xsi:type=\"xsd:string\">contenu</key>\n" +
                "            <value xsi:type=\"xsd:string\">$contenu</value>\n" +
                "          </item>\n" +
                "          <item>\n" +
                "            <key xsi:type=\"xsd:string\">poids</key>\n" +
                "            <value xsi:type=\"xsd:int\">$poids</value>\n" +
                "          </item>\n" +
                "         </fichier>\n" +
                "      </web:nouveau_fichier_log>\n" +
                "   </soapenv:Body>\n" +
                "</soapenv:Envelope>"

        wireMockServer.stubFor(post(WireMock.urlEqualTo("/webserv/?module=dce%7Cserveur_fichier_dce"))
                .withHeader("Content-Type", equalTo("text/plain;charset=UTF-8"))
                .withRequestBody(equalToXml(request))
                .willReturn(WireMock.aResponse().withBody("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ns1=\"https://www.marches-securises.fr/webserv/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:SOAP-ENC=\"http://schemas.xmlsoap.org/soap/encoding/\" SOAP-ENV:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n" +
                        "    <SOAP-ENV:Body>\n" +
                        "        <ns1:nouveau_fichier_logResponse>\n" +
                        "            <return xsi:type=\"xsd:string\"><?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<ifw:data xmlns:ifw=\"interbat/framwork-exportation\">\n" +
                        "  <objet type=\"ms_v2__fullweb_piece\">\n" +
                        "    <propriete nom=\"cle_piece\">$clePiece</propriete>\n" +
                        "    <propriete nom=\"cle_dce\">$dce</propriete>\n" +
                        "    <propriete nom=\"cle_lot\"/>\n" +
                        "    <propriete nom=\"libelle\">$libellePiece</propriete>\n" +
                        "    <propriete nom=\"ordre\">1</propriete>\n" +
                        "    <propriete nom=\"nom\">${nom}.${extension}</propriete>\n" +
                        "    <propriete nom=\"la\">$la</propriete>\n" +
                        "    <propriete nom=\"extention\">$extension</propriete>\n" +
                        "    <propriete nom=\"type_mime\">text/plain; charset=us-ascii</propriete>\n" +
                        "    <propriete nom=\"taille\">$poids</propriete>\n" +
                        "    <propriete nom=\"date_mise_en_ligne\">$datePublication</propriete>\n" +
                        "  </objet>\n" +
                        "  <objet type=\"ms_v2__fullweb_piece\">\n" +
                        "    <propriete nom=\"cle_piece\">1533648460539i8kwbgc8b</propriete>\n" +
                        "    <propriete nom=\"cle_dce\">$dce</propriete>\n" +
                        "    <propriete nom=\"cle_lot\"/>\n" +
                        "    <propriete nom=\"libelle\">libelle 2e piece</propriete>\n" +
                        "    <propriete nom=\"ordre\">2</propriete>\n" +
                        "    <propriete nom=\"nom\">FileName2.txt</propriete>\n" +
                        "    <propriete nom=\"la\">0</propriete>\n" +
                        "    <propriete nom=\"extention\">txt</propriete>\n" +
                        "    <propriete nom=\"type_mime\">text/plain; charset=us-ascii</propriete>\n" +
                        "    <propriete nom=\"taille\">4</propriete>\n" +
                        "    <propriete nom=\"date_mise_en_ligne\">1533648460</propriete>\n" +
                        "  </objet>\n" +
                        "  <objet type=\"ms_v2__fullweb_piece\">\n" +
                        "    <propriete nom=\"cle_piece\">1533648507r24pkejiej5v</propriete>\n" +
                        "    <propriete nom=\"cle_dce\">$dce</propriete>\n" +
                        "    <propriete nom=\"cle_lot\"/>\n" +
                        "    <propriete nom=\"libelle\">libelle 3e piece</propriete>\n" +
                        "    <propriete nom=\"ordre\">3</propriete>\n" +
                        "    <propriete nom=\"nom\">FileName3.txt</propriete>\n" +
                        "    <propriete nom=\"la\">0</propriete>\n" +
                        "    <propriete nom=\"extention\">txt</propriete>\n" +
                        "    <propriete nom=\"type_mime\">text/plain; charset=us-ascii</propriete>\n" +
                        "    <propriete nom=\"taille\">5</propriete>\n" +
                        "    <propriete nom=\"date_mise_en_ligne\">1533648507</propriete>\n" +
                        "  </objet>\n" +
                        "</ifw:data>\n" +
                        "</return>\n" +
                        "        </ns1:nouveau_fichier_logResponse>\n" +
                        "    </SOAP-ENV:Body>\n" +
                        "</SOAP-ENV:Envelope>").withStatus(200)))

        val entity = restTemplate.postForEntity<String>(pieceUrl, request)
        Assertions.assertThat(entity.statusCode).isEqualTo(HttpStatus.OK)

        val piece = Piece(uuid = UUID.randomUUID().toString(), uuidLot = null, libelle = libellePiece, aapc = false, ordre = ordreIntPiece, nom = nom, extension = extension, contenu = byteArrayContenu, poids = 6000000)

        val parsedClePiece = marcheSecuriseService.parseClePiece(entity.body.toString(), piece)
        Assertions.assertThat(parsedClePiece).isEqualTo(clePiece)

        WireMock.verify(WireMock.postRequestedFor(WireMock.urlEqualTo("/webserv/?module=dce%7Cserveur_fichier_dce")))
    }

    @Test
    fun parse_clePiece_from_incorrect_piece_creation_response(){
        val request = "<soapenv:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:web=\"https://www.marches-securises.fr/webserv/\" xmlns:s2=\"http://xml.apache.org/xml-soap\">\n" +
                "   <soapenv:Header/>\n" +
                "   <soapenv:Body>\n" +
                "      <web:nouveau_fichier_log soapenv:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n" +
                "         <login xsi:type=\"xsd:string\">$login</login>\n" +
                "         <mdp xsi:type=\"xsd:string\">$password</mdp>\n" +
                "         <pa xsi:type=\"xsd:string\">$pa</pa>\n" +
                "         <dce xsi:type=\"xsd:string\">WrongDce</dce>\n" +
                "         <fichier xsi:type=\"s2:Map\">\n" +
                "          <item>\n" +
                "            <key xsi:type=\"xsd:string\">lot</key>\n" +
                "            <value xsi:type=\"xsd:string\"></value>\n" +
                "          </item>\n" +
                "          <item>\n" +
                "            <key xsi:type=\"xsd:string\">libelle</key>\n" +
                "            <value xsi:type=\"xsd:string\">$libellePiece</value>\n" +
                "          </item>\n" +
                "          <item>\n" +
                "            <key xsi:type=\"xsd:string\">la</key>\n" +
                "            <value xsi:type=\"xsd:string\">$la</value>\n" +
                "          </item>\n" +
                "          <item>\n" +
                "            <key xsi:type=\"xsd:string\">ordre</key>\n" +
                "            <value xsi:type=\"xsd:int\">$ordrePiece</value>\n" +
                "          </item>\n" +
                "          <item>\n" +
                "            <key xsi:type=\"xsd:string\">nom</key>\n" +
                "            <value xsi:type=\"xsd:string\">$nom</value>\n" +
                "          </item>\n" +
                "          <item>\n" +
                "            <key xsi:type=\"xsd:string\">extension</key>\n" +
                "            <value xsi:type=\"xsd:string\">$extension</value>\n" +
                "          </item>\n" +
                "          <item>\n" +
                "            <key xsi:type=\"xsd:string\">contenu</key>\n" +
                "            <value xsi:type=\"xsd:string\">$contenu</value>\n" +
                "          </item>\n" +
                "          <item>\n" +
                "            <key xsi:type=\"xsd:string\">poids</key>\n" +
                "            <value xsi:type=\"xsd:int\">$poids</value>\n" +
                "          </item>\n" +
                "         </fichier>\n" +
                "      </web:nouveau_fichier_log>\n" +
                "   </soapenv:Body>\n" +
                "</soapenv:Envelope>"

        wireMockServer.stubFor(post(WireMock.urlEqualTo("/webserv/?module=dce%7Cserveur_fichier_dce"))
                .withHeader("Content-Type", equalTo("text/plain;charset=UTF-8"))
                .withRequestBody(equalToXml(request))
                .willReturn(WireMock.aResponse().withBody("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ns1=\"https://www.marches-securises.fr/webserv/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:SOAP-ENC=\"http://schemas.xmlsoap.org/soap/encoding/\" SOAP-ENV:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n" +
                        "    <SOAP-ENV:Body>\n" +
                        "        <ns1:nouveau_fichier_logResponse>\n" +
                        "            <return xsi:type=\"xsd:string\"><?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<creation_file_error xmlns=\"interbat/erreur_crea_file\"><consultation_non_trouvee cle_pa=\"1267898337p8xft\" cle_dce=\"WrongDce\" cle_piece=\"\" cle_lot=\"\" libelle=\"\" ordre=\"\" nom=\"\" extention=\"\" type_mime=\"\" taille=\"\" date_mise_en_ligne=\"\"/></creation_file_error>\n" +
                        "</return>\n" +
                        "        </ns1:nouveau_fichier_logResponse>\n" +
                        "    </SOAP-ENV:Body>\n" +
                        "</SOAP-ENV:Envelope>").withStatus(200)))

        val entity = restTemplate.postForEntity<String>(pieceUrl, request)
        Assertions.assertThat(entity.statusCode).isEqualTo(HttpStatus.OK)

        val piece = Piece(uuid = UUID.randomUUID().toString(), uuidLot = null, libelle = libellePiece, aapc = false, ordre = ordreIntPiece, nom = nom, extension = extension, contenu = byteArrayContenu, poids = 6000000)

        val parsedClePiece = marcheSecuriseService.parseClePiece(entity.body.toString(), piece)
        Assertions.assertThat(parsedClePiece).isEmpty()

        WireMock.verify(WireMock.postRequestedFor(WireMock.urlEqualTo("/webserv/?module=dce%7Cserveur_fichier_dce")))
    }

    @Test
    fun parse_clePiece_from_no_response_from_piece_creation_soap_request(){
        val request = "<soapenv:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:web=\"https://www.marches-securises.fr/webserv/\" xmlns:s2=\"http://xml.apache.org/xml-soap\">\n" +
                "   <soapenv:Header/>\n" +
                "   <soapenv:Body>\n" +
                "      <web:nouveau_fichier_log soapenv:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n" +
                "         <login xsi:type=\"xsd:string\">$login</login>\n" +
                "         <mdp xsi:type=\"xsd:string\">$password</mdp>\n" +
                "         <pa xsi:type=\"xsd:string\">$pa</pa>\n" +
                "         <dce xsi:type=\"xsd:string\">$dce</dce>\n" +
                "         <fichier xsi:type=\"s2:Map\">\n" +
                "          <item>\n" +
                "            <key xsi:type=\"xsd:string\">lot</key>\n" +
                "            <value xsi:type=\"xsd:string\"></value>\n" +
                "          </item>\n" +
                "          <item>\n" +
                "            <key xsi:type=\"xsd:string\">libelle</key>\n" +
                "            <value xsi:type=\"xsd:string\">$libellePiece</value>\n" +
                "          </item>\n" +
                "          <item>\n" +
                "            <key xsi:type=\"xsd:string\">la</key>\n" +
                "            <value xsi:type=\"xsd:string\">$la</value>\n" +
                "          </item>\n" +
                "          <item>\n" +
                "            <key xsi:type=\"xsd:string\">ordre</key>\n" +
                "            <value xsi:type=\"xsd:int\">$ordrePiece</value>\n" +
                "          </item>\n" +
                "          <item>\n" +
                "            <key xsi:type=\"xsd:string\">nom</key>\n" +
                "            <value xsi:type=\"xsd:string\">$nom</value>\n" +
                "          </item>\n" +
                "          <item>\n" +
                "            <key xsi:type=\"xsd:string\">extension</key>\n" +
                "            <value xsi:type=\"xsd:string\">$extension</value>\n" +
                "          </item>\n" +
                "          <item>\n" +
                "            <key xsi:type=\"xsd:string\">contenu</key>\n" +
                "            <value xsi:type=\"xsd:string\">$contenu</value>\n" +
                "          </item>\n" +
                "          <item>\n" +
                "            <key xsi:type=\"xsd:string\">poids</key>\n" +
                "            <value xsi:type=\"xsd:int\">$poids</value>\n" +
                "          </item>\n" +
                "         </fichier>\n" +
                "      </web:nouveau_fichier_log>\n" +
                "   </soapenv:Body>\n" +
                "</soapenv:Envelope>"

        wireMockServer.stubFor(post(WireMock.urlEqualTo("/webserv/?module=dce%7Cserveur_fichier_dce"))
                .withHeader("Content-Type", equalTo("text/plain;charset=UTF-8"))
                .withRequestBody(equalToXml(request))
                .willReturn(WireMock.aResponse().withFixedDelay(6000).withBody("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        " <SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\">\n" +
                        "     <SOAP-ENV:Body>\n" +
                        "        <SOAP-ENV:Fault>\n" +
                        "            <faultcode>SOAP-ENV:Server</faultcode>\n" +
                        "            <faultstring>Maximum execution time of 60 seconds exceeded</faultstring>\n" +
                        "        </SOAP-ENV:Fault>\n" +
                        "    </SOAP-ENV:Body>\n" +
                        "</SOAP-ENV:Envelope>").withStatus(500)))

        val entity = restTemplate.postForEntity<String>(pieceUrl, request)
        Assertions.assertThat(entity.statusCode).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)

        val piece = Piece(uuid = UUID.randomUUID().toString(), uuidLot = null, libelle = libellePiece, aapc = false, ordre = ordreIntPiece, nom = nom, extension = extension, contenu = byteArrayContenu, poids = 6000000)

        val parsedClePiece = marcheSecuriseService.parseClePiece(entity.body.toString(), piece)
        Assertions.assertThat(parsedClePiece).isEmpty()

        WireMock.verify(WireMock.postRequestedFor(WireMock.urlEqualTo("/webserv/?module=dce%7Cserveur_fichier_dce")))
    }
}