package org.ozwillo.dcimporter.handler

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import com.github.tomakehurst.wiremock.matching.EqualToPattern
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.ozwillo.dcimporter.config.RabbitMockConfig
import org.ozwillo.dcimporter.model.BusinessAppConfiguration
import org.ozwillo.dcimporter.model.BusinessMapping
import org.ozwillo.dcimporter.model.marchepublic.*
import org.ozwillo.dcimporter.repository.BusinessAppConfigurationRepository
import org.ozwillo.dcimporter.repository.BusinessMappingRepository
import org.ozwillo.dcimporter.service.MarcheSecuriseListingService
import org.ozwillo.dcimporter.service.MarcheSecuriseService
import org.ozwillo.dcimporter.util.MSUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.client.getForEntity
import org.springframework.boot.test.web.client.postForEntity
import org.springframework.context.annotation.Import
import org.springframework.http.*
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.LocalDateTime
import java.util.*

@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(RabbitMockConfig::class)
class MarchePublicHandlerTest(@Autowired val restTemplate: TestRestTemplate) {

    private lateinit var wireMockServer: WireMockServer

    private val siret = "123456789"
    private val referenceConsultation = "ref-consultation"
    private val uuidLot = UUID.randomUUID().toString()
    private val uuidPiece = UUID.randomUUID().toString()
    private val cleRegistre = "1533048729cetzyl2xvn78"

    private lateinit var businessMapping: BusinessMapping
    private lateinit var businessAppConfiguration: BusinessAppConfiguration
    @Autowired
    private lateinit var businessMappingRepository: BusinessMappingRepository
    @Autowired
    private lateinit var businessAppConfigurationRepository: BusinessAppConfigurationRepository

    private val tokenInfoResponse = """
        {
            "access_token": "secretToken",
            "expires_in": 3600,
            "scope": "datacore openid profile offline_access email",
            "token_type": "Bearer"
        }
        """

    private val dcGetAllConsultationResponse = """
        [
            {
                "@id": "http://data.ozwillo.com/dc/type/marchepublic:consultation_0/FR/$siret/$referenceConsultation",
                "mpconsultation:organization": "http://data.ozwillo.com/dc/type/orgfr:Organisation_0/FR/$siret"
            },
            {
                "@id": "http://data.ozwillo.com/dc/type/marchepublic:consultation_0/FR/$siret/$referenceConsultation-2",
                "mpconsultation:organization": "http://data.ozwillo.com/dc/type/orgfr:Organisation_0/FR/$siret"
            },
            {
                "@id": "http://data.ozwillo.com/dc/type/marchepublic:consultation_0/FR/$siret/$referenceConsultation-3",
                "mpconsultation:organization": "http://data.ozwillo.com/dc/type/orgfr:Organisation_0/FR/$siret"
            }
        ]
        """

    private val dcGetAllRegisterResponse = """
        [
            {
                "@id": "http://data.ozwillo.com/dc/type/marchepublic:reponse_0/FR/$siret/$referenceConsultation/$cleRegistre"
            }
        ]
        """

    private val dcGetRegisterResponse = """
        {
                "@id": "http://data.ozwillo.com/dc/type/marchepublic:reponse_0/FR/$siret/$referenceConsultation/$cleRegistre",
                "o:version": 0
        }
        """

    private val dcGetAllResponseEmpty = """
        []
        """

    private val dcGetTheOnlyConsultationResponse = """
        [
            {
                "@id": "http://data.ozwillo.com/dc/type/marchepublic:consultation_0/FR/$siret/$referenceConsultation",
                "mpconsultation:organization": "http://data.ozwillo.com/dc/type/orgfr:Organisation_0/FR/$siret"
            }
        ]
        """

    private val dcGetOrganizationResponse = """
        {
            "@id": "https://data.ozwillo.com/dc/type/orgfr:Organisation_0/FR/$siret"
        }
        """

    private val dcPostConsultationResponse = """
        {
            "@id": "http://data.ozwillo.com/dc/type/marchepublic:consultation_0/FR/$siret/$referenceConsultation"
        }
        """

    private val dcExistingResponse = """
            {
                "@id": "http://data.ozwillo.com/dc/type/marchepublic:consultation_0/FR/$siret/$referenceConsultation",
                "o:version": "0"
            }
            """

    val dcPostLotResponse = """
        {
            "@id": "http://data.ozwillo.com/dc/type/marchepublic:lot_0/FR/$siret/$referenceConsultation/$uuidLot"
        }
        """

    private val dcExistingLotResponse = """
            {
                "@id": "http://data.ozwillo.com/dc/type/marchepublic:lot_0/FR/$siret/$referenceConsultation/$uuidLot",
                "o:version": "0"
            }
            """

    private val msRegistreReponseXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ns1=\"https://www.marches-securises.fr/webserv/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:SOAP-ENC=\"http://schemas.xmlsoap.org/soap/encoding/\" SOAP-ENV:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n" +
            "    <SOAP-ENV:Body>\n" +
            "        <ns1:lister_reponses_electroniquesResponse>\n" +
            "            <return xsi:type=\"xsd:string\">&lt;?xml version=\"1.0\" encoding=\"UTF-8\"?&gt;\n" +
            "&lt;ifw:data xmlns:ifw=\"interbat/framwork-exportation\"&gt;\n" +
            "  &lt;objet type=\"ms__reponse\"&gt;\n" +
            "    &lt;propriete nom=\"cle\"&gt;$cleRegistre&lt;/propriete&gt;\n" +
            "    &lt;propriete nom=\"cle_dce\"&gt;dce&lt;/propriete&gt;\n" +
            "    &lt;propriete nom=\"cle_entreprise_ms\"&gt;cleEntreprise&lt;/propriete&gt;\n" +
            "    &lt;propriete nom=\"denomination_ent\"&gt;nomEntreprise&lt;/propriete&gt;\n" +
            "    &lt;propriete nom=\"contact\"&gt;contact&lt;/propriete&gt;\n" +
            "    &lt;propriete nom=\"email_contact\"&gt;emailContact&lt;/propriete&gt;\n" +
            "    &lt;propriete nom=\"date_depot\"&gt;1533300425&lt;/propriete&gt;\n" +
            "    &lt;propriete nom=\"date_depot_f\"&gt;vendredi 03 août 2018 - 14:47&lt;/propriete&gt;\n" +
            "  &lt;propriete nom=\"taille_reponse\"&gt;4105705&lt;/propriete&gt;&lt;objet type=\"entreprise\"&gt;\n" +
            "    &lt;propriete nom=\"nom\"&gt;nomEntreprise&lt;/propriete&gt;\n" +
            "    &lt;propriete nom=\"adresse_1\"&gt;adresse&lt;/propriete&gt;\n" +
            "    &lt;propriete nom=\"adresse_2\"/&gt;\n" +
            "    &lt;propriete nom=\"code_postal\"&gt;codePostal&lt;/propriete&gt;\n" +
            "    &lt;propriete nom=\"commune\"&gt;commune&lt;/propriete&gt;\n" +
            "    &lt;propriete nom=\"pays\"&gt;pays&lt;/propriete&gt;\n" +
            "    &lt;propriete nom=\"tel\"&gt;tel&lt;/propriete&gt;\n" +
            "    &lt;propriete nom=\"fax\"&gt;fax&lt;/propriete&gt;\n" +
            "    &lt;propriete nom=\"siret\"/&gt;\n" +
            "    &lt;propriete nom=\"siren\"/&gt;\n" +
            "    &lt;propriete nom=\"code_naf\"&gt;naf&lt;/propriete&gt;\n" +
            "    &lt;propriete nom=\"url\"&gt;url&lt;/propriete&gt;\n" +
            "  &lt;/objet&gt;&lt;/objet&gt;\n" +
            "&lt;pagination ordre=\"\" sensordre=\"ASC\"/&gt;&lt;reponses nb_total=\"1\"/&gt;&lt;/ifw:data&gt;\n" +
            "</return>\n" +
            "        </ns1:lister_reponses_electroniquesResponse>\n" +
            "    </SOAP-ENV:Body>\n" +
            "</SOAP-ENV:Envelope>"

    @BeforeAll
    fun beforeAll() {
        wireMockServer = WireMockServer(wireMockConfig().port(8089))
        wireMockServer.start()

        // we need a fake bearer to go through the verification chain
        restTemplate.restTemplate.interceptors.add(ClientHttpRequestInterceptor { request, body, execution ->
            request.headers["Authorization"] = "Bearer secrettoken"
            execution.execute(request, body)
        })

        WireMock.configureFor(8089)

        businessMapping = BusinessMapping(id = "testId", dcId = "http://data.ozwillo.com/dc/type/marchepublic:consultation_0/FR/$siret/$referenceConsultation", businessId = "businessIdTest", businessId2 = "msReference", applicationName = MarcheSecuriseService.name, type = MSUtils.CONSULTATION_TYPE)
        businessAppConfiguration = BusinessAppConfiguration(applicationName = MarcheSecuriseService.name, baseUrl = "http://localhost:8089", organizationSiret = siret, instanceId = "pa", password = "password", login = "login")
        businessMappingRepository.save(businessMapping).subscribe()
        businessAppConfigurationRepository.save(businessAppConfiguration).subscribe()
    }

    @AfterAll
    fun afterAll() {
        wireMockServer.stop()
    }

    @Test
    fun `Test correct get consultations list for given Org`(){
        stubFor(WireMock.get(WireMock.urlMatching("/dc/type/orgfr:Organisation_0/FR/$siret"))
                .willReturn(WireMock.okJson(dcGetOrganizationResponse).withStatus(200)))
        stubFor(WireMock.post(WireMock.urlMatching("/a/token"))
                .withHeader("Authorization", EqualToPattern("Basic ZGNpbXBvcnRlcjpzZWNyZXQ="))
                .willReturn(WireMock.okJson(tokenInfoResponse).withStatus(200)))
        stubFor(WireMock.get(WireMock.urlMatching("/dc/type/marchepublic:consultation_0\\?start=0&limit=50&mpconsultation:organization=https://data.ozwillo.com/dc/type/orgfr:Organisation_0/FR/$siret-"))
                .willReturn(WireMock.okJson(dcGetAllConsultationResponse).withStatus(200)))


        val entity = restTemplate.getForEntity("/api/marche-public/$siret/consultation", String::class.java)
        assertThat(entity.statusCode).isEqualTo(HttpStatus.OK)
        val resourceList = entity.body!!.split("},{")
        assertThat(resourceList.size).isEqualTo(3)
    }

    @Test
    fun `Test rejection of get consultations list for given Org because Org do not exist in dc`(){
        stubFor(WireMock.get(WireMock.urlMatching("/dc/type/orgfr:Organisation_0/FR/$siret"))
                .willReturn(aResponse().withStatus(404)))

        val entity = restTemplate.getForEntity("/api/marche-public/$siret/consultation", String::class.java)
        assertThat(entity.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
        assertThat(entity.body).isEqualTo("Organisation with siret $siret does not exist")
    }

    @Test
    fun `Test correct get empty consultations list for given Org`(){
        stubFor(WireMock.get(WireMock.urlMatching("/dc/type/orgfr:Organisation_0/FR/$siret"))
                .willReturn(WireMock.okJson(dcGetOrganizationResponse).withStatus(200)))
        stubFor(WireMock.post(WireMock.urlMatching("/a/token"))
                .withHeader("Authorization", EqualToPattern("Basic ZGNpbXBvcnRlcjpzZWNyZXQ="))
                .willReturn(WireMock.okJson(tokenInfoResponse).withStatus(200)))
        stubFor(WireMock.get(WireMock.urlMatching("/dc/type/marchepublic:consultation_0\\?start=0&limit=50&mpconsultation:organization=https://data.ozwillo.com/dc/type/orgfr:Organisation_0/FR/$siret-"))
                .willReturn(WireMock.okJson(dcGetAllResponseEmpty).withStatus(200)))


        val entity = restTemplate.getForEntity("/api/marche-public/$siret/consultation", String::class.java)
        assertThat(entity.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(entity.body).isEqualTo("[]")
    }

    @Test
    fun `Test correct get the only consultation for given Org`(){
        stubFor(WireMock.get(WireMock.urlMatching("/dc/type/orgfr:Organisation_0/FR/$siret"))
                .willReturn(WireMock.okJson(dcGetOrganizationResponse).withStatus(200)))
        stubFor(WireMock.post(WireMock.urlMatching("/a/token"))
                .withHeader("Authorization", EqualToPattern("Basic ZGNpbXBvcnRlcjpzZWNyZXQ="))
                .willReturn(WireMock.okJson(tokenInfoResponse).withStatus(200)))
        stubFor(WireMock.get(WireMock.urlMatching("/dc/type/marchepublic:consultation_0\\?start=0&limit=50&mpconsultation:organization=https://data.ozwillo.com/dc/type/orgfr:Organisation_0/FR/$siret-"))
                .willReturn(WireMock.okJson(dcGetTheOnlyConsultationResponse).withStatus(200)))


        val entity = restTemplate.getForEntity("/api/marche-public/$siret/consultation", String::class.java)
        assertThat(entity.statusCode).isEqualTo(HttpStatus.OK)
        val resourceList = entity.body!!.split("},{")
        assertThat(resourceList.size).isEqualTo(1)
    }

    @Test
    fun `Test correct creation of a consultation`() {
        stubFor(WireMock.get(WireMock.urlMatching("/dc/type/orgfr:Organisation_0/FR/$siret"))
                .willReturn(WireMock.okJson(dcGetOrganizationResponse).withStatus(200)))
        stubFor(WireMock.get(WireMock.urlMatching("/dc/type/marchepublic:consultation_0/FR/$siret/$referenceConsultation"))
                .willReturn(WireMock.aResponse().withStatus(404)))
        stubFor(WireMock.post(WireMock.urlMatching("/dc/type/marchepublic:consultation_0"))
                .willReturn(WireMock.okJson(dcPostConsultationResponse).withStatus(201)))

        val consultation = Consultation(reference = referenceConsultation,
                objet = "mon marche", datePublication = LocalDateTime.now(), dateCloture = LocalDateTime.now(),
                finaliteMarche = FinaliteMarcheType.MARCHE, typeMarche = TypeMarcheType.PUBLIC,
                typePrestation = TypePrestationType.FOURNITURES, departementsPrestation = listOf(6, 83),
                passation = "passation", informatique = true, passe = "motdepasse", emails = listOf("dev@sictiam.fr", "demat@sictiam.fr"),
                enLigne = false, alloti = false, invisible = false, nbLots = 1)

        val entity = restTemplate.postForEntity<String>("/api/marche-public/$siret/consultation", consultation)
        assertThat(entity.statusCode).isEqualTo(HttpStatus.CREATED)
        assertThat(entity.headers["Location"]).isEqualTo(listOf("http://localhost:3000/api/marche-public/$siret/consultation/$referenceConsultation"))

        WireMock.verify(WireMock.postRequestedFor(WireMock.urlEqualTo("/dc/type/marchepublic:consultation_0")))
    }

    @Test
    fun `Test rejection of creation of consultation because uri already exist`() {
        stubFor(WireMock.get(WireMock.urlMatching("/dc/type/orgfr:Organisation_0/FR/$siret"))
                .willReturn(WireMock.okJson(dcGetOrganizationResponse).withStatus(200)))
        stubFor(WireMock.get(WireMock.urlMatching("/dc/type/marchepublic:consultation_0/FR/$siret/$referenceConsultation"))
                .willReturn(WireMock.okJson(dcExistingResponse).withStatus(200)))
        stubFor(WireMock.post(WireMock.urlMatching("/dc/type/marchepublic:consultation_0"))
                .willReturn(WireMock.okJson(dcPostConsultationResponse).withStatus(400)))

        val consultation = Consultation(reference = referenceConsultation,
                objet = "mon marche", datePublication = LocalDateTime.now(), dateCloture = LocalDateTime.now(),
                finaliteMarche = FinaliteMarcheType.MARCHE, typeMarche = TypeMarcheType.PUBLIC,
                typePrestation = TypePrestationType.FOURNITURES, departementsPrestation = listOf(6, 83),
                passation = "passation", informatique = true, passe = "motdepasse", emails = listOf("dev@sictiam.fr", "demat@sictiam.fr"),
                enLigne = false, alloti = false, invisible = false, nbLots = 1)

        val entity = restTemplate.postForEntity<String>("/api/marche-public/$siret/consultation", consultation)
        assertThat(entity.statusCode).isEqualTo(HttpStatus.CONFLICT)
        assertThat(entity.headers["Location"]).isNull()

        WireMock.verify(WireMock.postRequestedFor(WireMock.urlEqualTo("/dc/type/marchepublic:consultation_0")))
    }

    @Test
    fun `Test bad request sent to the Datacore`() {
        stubFor(WireMock.get(WireMock.urlMatching("/dc/type/orgfr:Organisation_0/FR/$siret"))
                .willReturn(WireMock.okJson(dcGetOrganizationResponse).withStatus(200)))
        stubFor(WireMock.get(WireMock.urlMatching("/dc/type/marchepublic:consultation_0/FR/$siret/$referenceConsultation"))
                .willReturn(WireMock.aResponse().withStatus(404)))
        stubFor(WireMock.post(WireMock.urlMatching("/dc/type/marchepublic:consultation_0"))
                .willReturn(WireMock.okJson(dcPostConsultationResponse).withStatus(400)))

        val consultation = Consultation(reference = referenceConsultation,
                objet = "mon marche", datePublication = LocalDateTime.now(), dateCloture = LocalDateTime.now(),
                finaliteMarche = FinaliteMarcheType.MARCHE, typeMarche = TypeMarcheType.PUBLIC,
                typePrestation = TypePrestationType.FOURNITURES, departementsPrestation = listOf(6, 83),
                passation = "passation", informatique = true, passe = "motdepasse", emails = listOf("dev@sictiam.fr", "demat@sictiam.fr"),
                enLigne = false, alloti = false, invisible = false, nbLots = 1)

        val entity = restTemplate.postForEntity<String>("/api/marche-public/$siret/consultation", consultation)
        assertThat(entity.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
        assertThat(entity.headers["Location"]).isNull()

        WireMock.verify(WireMock.postRequestedFor(WireMock.urlEqualTo("/dc/type/marchepublic:consultation_0")))
    }

    @Test
    fun `Test illformed consultation payload`() {
        val consultationJson = """
            {
                "reference": "reference",
                "dateCloture": "2018-05-31T00:00:00",
                "finaliteMarche": "MARCHE",
                "typePrestation": "TRAVAUX",
                "departementsPrestation": [6,83],
                "passation": "Passation",
                "informatique": "true",
                "emails": ["dev@sictiam.fr", "demat@sictiam.fr"],
                "enLigne": "false",
                "alloti": "true",
                "invisible": "false",
                "nbLots": "1"
            }
            """
        stubFor(WireMock.get(WireMock.urlMatching("/dc/type/orgfr:Organisation_0/FR/$siret"))
                .willReturn(WireMock.okJson(dcGetOrganizationResponse).withStatus(200)))

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        val entity = HttpEntity(consultationJson, headers)

        val response = restTemplate.exchange("/api/marche-public/$siret/consultation", HttpMethod.POST, entity, String::class.java)
        assertThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
        assertThat(response.body).startsWith("JSON decoding error")
    }

    @Test
    fun `Test correct update of a consultation`() {

        stubFor(WireMock.get(WireMock.urlMatching("/dc/type/orgfr:Organisation_0/FR/$siret"))
                .willReturn(WireMock.okJson(dcGetOrganizationResponse).withStatus(200)))
        stubFor(WireMock.post(WireMock.urlMatching("/dc/type/marchepublic:consultation_0"))
                .willReturn(WireMock.okJson(dcPostConsultationResponse).withStatus(201)))
        stubFor(WireMock.get(WireMock.urlMatching("/dc/type/marchepublic:consultation_0/FR/$siret/$referenceConsultation"))
                .willReturn(WireMock.okJson(dcExistingResponse).withStatus(200)))
        stubFor(WireMock.put(WireMock.urlMatching("/dc/type/marchepublic:consultation_0"))
                .willReturn(WireMock.okJson(dcPostConsultationResponse).withStatus(200)))

        val consultation = Consultation(reference = referenceConsultation,
                objet = "mon marche", datePublication = LocalDateTime.now(), dateCloture = LocalDateTime.now(),
                finaliteMarche = FinaliteMarcheType.MARCHE, typeMarche = TypeMarcheType.PUBLIC,
                typePrestation = TypePrestationType.FOURNITURES, departementsPrestation = listOf(6, 83),
                passation = "passation", informatique = true, passe = "motdepasse", emails = listOf("dev@sictiam.fr", "demat@sictiam.fr"),
                enLigne = false, alloti = false, invisible = false, nbLots = 1)

        val entity = restTemplate.postForEntity<String>("/api/marche-public/$siret/consultation", consultation)
        assertThat(entity.statusCode).isEqualTo(HttpStatus.CREATED)
        assertThat(entity.headers["Location"]).isEqualTo(listOf("http://localhost:3000/api/marche-public/$siret/consultation/$referenceConsultation"))

        val updatedConsultation = consultation.copy(objet = "mon nouvel objet", typeMarche = TypeMarcheType.PRIVE)
        val httpEntity = HttpEntity(updatedConsultation)
        val updatedEntity = restTemplate.exchange("/api/marche-public/$siret/consultation/$referenceConsultation",
                HttpMethod.PUT, httpEntity, String::class.java)
        assertThat(updatedEntity.statusCode).isEqualTo(HttpStatus.OK)

        WireMock.verify(WireMock.postRequestedFor(WireMock.urlEqualTo("/dc/type/marchepublic:consultation_0")))
        WireMock.verify(WireMock.putRequestedFor(WireMock.urlEqualTo("/dc/type/marchepublic:consultation_0")))
    }

    @Test
    fun `Test correct delete of a consultation`() {
        val dcGetResponse = """
            {
                "@id": "http://data.ozwillo.com/dc/type/marchepublic:consultation_0/FR/$siret/$referenceConsultation",
                "o:version": 1
            }
            """
        stubFor(WireMock.get(WireMock.urlMatching("/dc/type/orgfr:Organisation_0/FR/$siret"))
                .willReturn(WireMock.okJson(dcGetOrganizationResponse).withStatus(200)))
        stubFor(WireMock.post(WireMock.urlMatching("/dc/type/marchepublic:consultation_0"))
                .willReturn(WireMock.okJson(dcPostConsultationResponse).withStatus(201)))
        stubFor(WireMock.get(WireMock.urlMatching("/dc/type/marchepublic:consultation_0/FR/$siret/$referenceConsultation"))
                .willReturn(WireMock.okJson(dcGetResponse).withStatus(200)))
        stubFor(WireMock.delete(WireMock.urlMatching("/dc/type/marchepublic:consultation_0/FR/$siret/$referenceConsultation"))
                .willReturn(WireMock.aResponse().withStatus(204)))

        val consultation = Consultation(reference = referenceConsultation,
                objet = "mon marche", datePublication = LocalDateTime.now(), dateCloture = LocalDateTime.now(),
                finaliteMarche = FinaliteMarcheType.MARCHE, typeMarche = TypeMarcheType.PUBLIC,
                typePrestation = TypePrestationType.FOURNITURES, departementsPrestation = listOf(6, 83),
                passation = "passation", informatique = true, passe = "motdepasse", emails = listOf("dev@sictiam.fr", "demat@sictiam.fr"),
                enLigne = false, alloti = false, invisible = false, nbLots = 1)

        restTemplate.postForEntity<String>("/api/marche-public/$siret/consultation", consultation)
        restTemplate.delete("/api/marche-public/$siret/consultation/$referenceConsultation")

        WireMock.verify(WireMock.postRequestedFor(WireMock.urlEqualTo("/dc/type/marchepublic:consultation_0")))
        WireMock.verify(
                WireMock.deleteRequestedFor(
                        WireMock.urlEqualTo("/dc/type/marchepublic:consultation_0/FR/$siret/$referenceConsultation"))
                        .withHeader("If-Match", EqualToPattern("1")))
    }

    @Test
    fun `Test correct publication of a consultation`(){
        stubFor(WireMock.get(WireMock.urlMatching("/dc/type/marchepublic:consultation_0/FR/$siret/$referenceConsultation"))
                .willReturn(WireMock.okJson(dcExistingResponse).withStatus(200)))
        stubFor(WireMock.get(WireMock.urlMatching("/dc/type/orgfr:Organisation_0/FR/$siret"))
                .willReturn(WireMock.okJson(dcGetOrganizationResponse).withStatus(200)))
        stubFor(WireMock.post(WireMock.urlMatching("/dc/type/marchepublic:consultation_0"))
                .willReturn(WireMock.okJson(dcPostConsultationResponse).withStatus(201)))
        stubFor(WireMock.get(WireMock.urlMatching("/dc/type/marchepublic:consultation_0/FR/$siret/$referenceConsultation"))
                .willReturn(WireMock.okJson(dcExistingResponse).withStatus(200)))
        stubFor(WireMock.put(WireMock.urlMatching("/dc/type/marchepublic:consultation_0"))
                .willReturn(WireMock.okJson(dcPostConsultationResponse).withStatus(200)))

        val entity = restTemplate.postForEntity<String>("/api/marche-public/$siret/consultation/$referenceConsultation/publish")
        assertThat(entity.statusCode).isEqualTo(HttpStatus.OK)

        WireMock.verify(WireMock.getRequestedFor(WireMock.urlEqualTo("/dc/type/marchepublic:consultation_0/FR/$siret/$referenceConsultation")))
    }

   @Test
    fun `Test correct creation of a lot`(){

        stubFor(WireMock.get(WireMock.urlMatching("/dc/type/orgfr:Organisation_0/FR/$siret"))
                .willReturn(WireMock.okJson(dcGetOrganizationResponse).withStatus(200)))
        stubFor(WireMock.get(WireMock.urlMatching("/dc/type/marchepublic:consultation_0/FR/$siret/$referenceConsultation"))
                .willReturn(WireMock.okJson(dcExistingResponse).withStatus(200)))
        stubFor(WireMock.post(WireMock.urlMatching("/dc/type/marchepublic:lot_0"))
                .willReturn(WireMock.okJson(dcPostLotResponse).withStatus(201)))

        val lot = Lot(uuid = uuidLot, libelle = "Lot de test", ordre = 1, numero = 1)
        val entity = restTemplate.postForEntity<String>("/api/marche-public/$siret/consultation/$referenceConsultation/lot", lot)
        assertThat(entity.statusCode).isEqualTo(HttpStatus.CREATED)
        assertThat(entity.headers["Location"]).isEqualTo(listOf("http://localhost:3000/api/marche-public/$siret/consultation/$referenceConsultation/lot/${lot.uuid}"))

        WireMock.verify(WireMock.postRequestedFor(WireMock.urlEqualTo("/dc/type/marchepublic:lot_0")))
    }

    @Test
    fun `Test bad request sent to datacore during creation of a lot`(){

        stubFor(WireMock.get(WireMock.urlMatching("/dc/type/orgfr:Organisation_0/FR/$siret"))
                .willReturn(WireMock.okJson(dcGetOrganizationResponse).withStatus(200)))
        stubFor(WireMock.get(WireMock.urlMatching("/dc/type/marchepublic:consultation_0/FR/$siret/$referenceConsultation"))
                .willReturn(WireMock.okJson(dcExistingResponse).withStatus(200)))
        stubFor(WireMock.post(WireMock.urlMatching("/dc/type/marchepublic:lot_0"))
                .willReturn(WireMock.okJson(dcPostLotResponse).withStatus(400)))

        val lot = Lot(uuid = uuidLot, libelle = "Lot de test", ordre = 1, numero = 1)
        val entity = restTemplate.postForEntity<String>("/api/marche-public/$siret/consultation/$referenceConsultation/lot", lot)
        assertThat(entity.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
        assertThat(entity.headers["Location"]).isNull()

        WireMock.verify(WireMock.postRequestedFor(WireMock.urlEqualTo("/dc/type/marchepublic:lot_0")))
    }

    @Test
    fun `Test correct update of a lot`(){
        stubFor(WireMock.get(WireMock.urlMatching("/dc/type/orgfr:Organisation_0/FR/$siret"))
                .willReturn(WireMock.okJson(dcGetOrganizationResponse).withStatus(200)))
        stubFor(WireMock.get(WireMock.urlMatching("/dc/type/marchepublic:consultation_0/FR/$siret/$referenceConsultation"))
                .willReturn(WireMock.okJson(dcExistingResponse).withStatus(200)))
        stubFor(WireMock.post(WireMock.urlMatching("/dc/type/marchepublic:lot_0"))
                .willReturn(WireMock.okJson(dcPostLotResponse).withStatus(201)))
        stubFor(WireMock.get(WireMock.urlMatching("/dc/type/marchepublic:lot_0/FR/$siret/$referenceConsultation/$uuidLot"))
                .willReturn(WireMock.okJson(dcExistingLotResponse).withStatus(200)))
        stubFor(WireMock.put(WireMock.urlMatching("/dc/type/marchepublic:lot_0"))
                .willReturn(WireMock.okJson(dcPostLotResponse).withStatus(200)))

        val lot = Lot(uuid = uuidLot, libelle = "Lot de test", ordre = 1, numero = 1)

        val entity = restTemplate.postForEntity<String>("/api/marche-public/$siret/consultation/$referenceConsultation/lot", lot)
        assertThat(entity.statusCode).isEqualTo(HttpStatus.CREATED)
        assertThat(entity.headers["Location"]).isEqualTo(listOf("http://localhost:3000/api/marche-public/$siret/consultation/$referenceConsultation/lot/${lot.uuid}"))

        val updatedLot = lot.copy(libelle = "mon nouveau libellé")
        val httpEntity = HttpEntity(updatedLot)
        val updatedEntity = restTemplate.exchange("/api/marche-public/$siret/consultation/$referenceConsultation/lot/$uuidLot",
                HttpMethod.PUT, httpEntity, String::class.java)
        assertThat(updatedEntity.statusCode).isEqualTo(HttpStatus.OK)

        WireMock.verify(WireMock.postRequestedFor(WireMock.urlEqualTo("/dc/type/marchepublic:lot_0")))
        WireMock.verify(WireMock.putRequestedFor(WireMock.urlEqualTo("/dc/type/marchepublic:lot_0")))

    }

    @Test
    fun `Test correct delete of a lot`(){

        val dcGetResponse = """
            {
                "@id" : "http://data.ozwillo.com/dc/type/marchepublic:lot_0/FR/$siret/$referenceConsultation/$uuidLot",
                "o:version" : 1
            }
            """

        stubFor(WireMock.get(WireMock.urlMatching("/dc/type/orgfr:Organisation_0/FR/$siret"))
                .willReturn(WireMock.okJson(dcGetOrganizationResponse).withStatus(200)))
        stubFor(WireMock.get(WireMock.urlMatching("/dc/type/marchepublic:consultation_0/FR/$siret/$referenceConsultation"))
                .willReturn(WireMock.okJson(dcExistingResponse).withStatus(200)))
        stubFor(WireMock.post(WireMock.urlMatching("/dc/type/marchepublic:lot_0"))
                .willReturn(WireMock.okJson(dcPostLotResponse).withStatus(201)))
        stubFor(WireMock.get(WireMock.urlMatching("/dc/type/marchepublic:lot_0/FR/$siret/$referenceConsultation/$uuidLot"))
                .willReturn(WireMock.okJson(dcGetResponse).withStatus(200)))
        stubFor(WireMock.delete(WireMock.urlMatching("/dc/type/marchepublic:lot_0/FR/$siret/$referenceConsultation/$uuidLot"))
                .willReturn(WireMock.aResponse().withStatus(204)))

        val lot = Lot(uuid = uuidLot, libelle = "Lot de test", ordre = 1, numero = 1)
        restTemplate.postForEntity<String>("/api/marche-public/$siret/consultation/$referenceConsultation/lot", lot)
        restTemplate.delete("/api/marche-public/$siret/consultation/$referenceConsultation/lot/$uuidLot")


        WireMock.verify(WireMock.postRequestedFor(WireMock.urlEqualTo("/dc/type/marchepublic:lot_0")))
        WireMock.verify(
                WireMock.deleteRequestedFor(
                        WireMock.urlEqualTo("/dc/type/marchepublic:lot_0/FR/$siret/$referenceConsultation/$uuidLot"))
                        .withHeader("If-Match", EqualToPattern("1")))
    }

    @Test
    fun `get response register list from datacore`(){

        stubFor(WireMock.get(WireMock.urlMatching("/dc/type/orgfr:Organisation_0/FR/$siret"))
                .willReturn(WireMock.okJson(dcGetOrganizationResponse).withStatus(200)))
        stubFor(WireMock.get(WireMock.urlMatching("/dc/type/marchepublic:consultation_0/FR/$siret/$referenceConsultation"))
                .willReturn(WireMock.okJson(dcExistingResponse).withStatus(200)))
        stubFor(WireMock.post(WireMock.urlMatching("/a/token"))
                .withHeader("Authorization", EqualToPattern("Basic ZGNpbXBvcnRlcjpzZWNyZXQ="))
                .willReturn(WireMock.okJson(tokenInfoResponse).withStatus(200)))
        stubFor(WireMock.get(WireMock.urlMatching("/dc/type/marchepublic:reponse_0\\?start=0&limit=50&mpreponse:consultation=http://data.ozwillo.com/dc/type/marchepublic:consultation_0/FR/$siret/$referenceConsultation-"))
                .willReturn(WireMock.okJson(dcGetAllRegisterResponse).withStatus(200)))


        val entity = restTemplate.getForEntity<String>("/api/marche-public/$siret/registre/type/${MSUtils.RESPONSE_TYPE}/$referenceConsultation")
        assertThat(entity.body).contains(cleRegistre)

        WireMock.verify(WireMock.getRequestedFor(WireMock.urlMatching("/dc/type/marchepublic:reponse_0\\?start=0&limit=50&mpreponse:consultation=http://data.ozwillo.com/dc/type/marchepublic:consultation_0/FR/$siret/$referenceConsultation-")))

    }

    @Test
    fun `get response register empty list from datacore`(){

        stubFor(WireMock.get(WireMock.urlMatching("/dc/type/orgfr:Organisation_0/FR/$siret"))
                .willReturn(WireMock.okJson(dcGetOrganizationResponse).withStatus(200)))
        stubFor(WireMock.get(WireMock.urlMatching("/dc/type/marchepublic:consultation_0/FR/$siret/$referenceConsultation"))
                .willReturn(WireMock.okJson(dcExistingResponse).withStatus(200)))
        stubFor(WireMock.post(WireMock.urlMatching("/a/token"))
                .withHeader("Authorization", EqualToPattern("Basic ZGNpbXBvcnRlcjpzZWNyZXQ="))
                .willReturn(WireMock.okJson(tokenInfoResponse).withStatus(200)))
        stubFor(WireMock.get(WireMock.urlMatching("/dc/type/marchepublic:reponse_0\\?start=0&limit=50&mpreponse:consultation=http://data.ozwillo.com/dc/type/marchepublic:consultation_0/FR/$siret/$referenceConsultation-"))
                .willReturn(WireMock.okJson(dcGetAllResponseEmpty).withStatus(200)))


        val entity = restTemplate.getForEntity<String>("/api/marche-public/$siret/registre/type/${MSUtils.RESPONSE_TYPE}/$referenceConsultation")
        assertThat(entity.body).isEqualTo("[]")

        WireMock.verify(WireMock.getRequestedFor(WireMock.urlMatching("/dc/type/marchepublic:reponse_0\\?start=0&limit=50&mpreponse:consultation=http://data.ozwillo.com/dc/type/marchepublic:consultation_0/FR/$siret/$referenceConsultation-")))

    }

    @Test
    fun `test datacore register response create for given consultation`(){
        stubFor(WireMock.get(WireMock.urlMatching("/dc/type/orgfr:Organisation_0/FR/$siret"))
                .willReturn(WireMock.okJson(dcGetOrganizationResponse).withStatus(200)))
        stubFor(WireMock.get(WireMock.urlMatching("/dc/type/marchepublic:consultation_0/FR/$siret/$referenceConsultation"))
                .willReturn(WireMock.okJson(dcExistingResponse).withStatus(200)))
        stubFor(WireMock.post(WireMock.urlMatching("/webserv/\\?module=registres%7Cserveur_registres"))
                .willReturn(WireMock.okTextXml(msRegistreReponseXml).withStatus(200)))
        stubFor(WireMock.get(WireMock.urlMatching("/dc/type/marchepublic:reponse_0/FR/$siret/$referenceConsultation/$cleRegistre"))
                .willReturn(WireMock.aResponse().withStatus(404)))
        stubFor(WireMock.post(WireMock.urlMatching("/dc/type/marchepublic:reponse_0"))
                .willReturn(WireMock.okJson(dcGetRegisterResponse).withStatus(201)))

        val entity = restTemplate.postForEntity<String>("/api/marche-public/$siret/registre/type/${MSUtils.RESPONSE_TYPE}/$referenceConsultation")

        assertThat(entity.statusCode).isEqualTo(HttpStatus.OK)

        WireMock.verify(WireMock.postRequestedFor(WireMock.urlMatching("/dc/type/marchepublic:reponse_0")))
    }

    @Test
    fun `test datacore register response update for given consultation`(){
        stubFor(WireMock.get(WireMock.urlMatching("/dc/type/orgfr:Organisation_0/FR/$siret"))
                .willReturn(WireMock.okJson(dcGetOrganizationResponse).withStatus(200)))
        stubFor(WireMock.get(WireMock.urlMatching("/dc/type/marchepublic:consultation_0/FR/$siret/$referenceConsultation"))
                .willReturn(WireMock.okJson(dcExistingResponse).withStatus(200)))
        stubFor(WireMock.post(WireMock.urlMatching("/webserv/\\?module=registres%7Cserveur_registres"))
                .willReturn(WireMock.okTextXml(msRegistreReponseXml).withStatus(200)))
        stubFor(WireMock.get(WireMock.urlMatching("/dc/type/marchepublic:reponse_0/FR/$siret/$referenceConsultation/$cleRegistre"))
                .willReturn(WireMock.okJson(dcGetRegisterResponse).withStatus(200)))
        stubFor(WireMock.put(WireMock.urlMatching("/dc/type/marchepublic:reponse_0"))
                .willReturn(WireMock.okJson(dcGetRegisterResponse).withStatus(200)))

        val entity = restTemplate.postForEntity<String>("/api/marche-public/$siret/registre/type/${MSUtils.RESPONSE_TYPE}/$referenceConsultation")

        assertThat(entity.statusCode).isEqualTo(HttpStatus.OK)

        WireMock.verify(WireMock.putRequestedFor(WireMock.urlMatching("/dc/type/marchepublic:reponse_0")))
    }
}