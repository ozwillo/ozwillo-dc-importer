package org.ozwillo.dcimporter.handler

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import com.github.tomakehurst.wiremock.matching.EqualToPattern
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.ozwillo.dcimporter.config.RabbitMockConfig
import org.ozwillo.dcimporter.model.BusinessAppConfiguration
import org.ozwillo.dcimporter.model.BusinessMapping
import org.ozwillo.dcimporter.model.marchepublic.*
import org.ozwillo.dcimporter.repository.BusinessAppConfigurationRepository
import org.ozwillo.dcimporter.repository.BusinessMappingRepository
import org.ozwillo.dcimporter.service.MarcheSecuriseService
import org.ozwillo.dcimporter.util.MSUtils
import org.ozwillo.dcimporter.utils.DCReturnModel
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.client.getForEntity
import org.springframework.boot.test.web.client.postForEntity
import org.springframework.context.annotation.Import
import org.springframework.http.*
import org.springframework.http.client.ClientHttpRequestInterceptor
import java.time.LocalDateTime

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(RabbitMockConfig::class)
class MarchePublicHandlerTest(@Autowired val restTemplate: TestRestTemplate) {

    private lateinit var wireMockServer: WireMockServer

    private lateinit var businessMapping: BusinessMapping
    private lateinit var businessAppConfiguration: BusinessAppConfiguration
    @Autowired
    private lateinit var businessMappingRepository: BusinessMappingRepository
    @Autowired
    private lateinit var businessAppConfigurationRepository: BusinessAppConfigurationRepository

    private val siret = DCReturnModel.siret
    private val referenceConsultation = DCReturnModel.referenceConsultation
    private val uuidLot = DCReturnModel.uuidLot
    private val uuidPiece = DCReturnModel.uuidPiece
    private val cleRegistre = DCReturnModel.cleRegistre
    private val clePersonne = DCReturnModel.clePersonne


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

        businessMapping = BusinessMapping(
            id = "testId",
            dcId = "http://data.ozwillo.com/dc/type/marchepublic:consultation_0/FR/$siret/$referenceConsultation",
            businessId = "businessIdTest",
            businessId2 = "msReference",
            applicationName = MarcheSecuriseService.name,
            type = MSUtils.CONSULTATION_TYPE
        )
        businessAppConfiguration = BusinessAppConfiguration(
            applicationName = MarcheSecuriseService.name,
            displayName = "Marchés Sécurisés",
            baseUrl = "http://localhost:8089",
            organizationSiret = siret,
            instanceId = "pa",
            password = "password",
            login = "login"
        )
        businessMappingRepository.save(businessMapping).subscribe()
        businessAppConfigurationRepository.save(businessAppConfiguration).subscribe()
    }

    @AfterAll
    fun afterAll() {
        wireMockServer.stop()
    }

    @Test
    fun `Test correct get consultations list for given Org`() {
        stubFor(
            WireMock.get(WireMock.urlMatching("/dc/type/orgfr:Organisation_0/FR/$siret"))
                .willReturn(WireMock.okJson(DCReturnModel.dcGetOrganizationResponse).withStatus(200))
        )
        stubFor(
            WireMock.post(WireMock.urlMatching("/a/token"))
                .withHeader("Authorization", EqualToPattern("Basic ZGNpbXBvcnRlcjpzZWNyZXQ="))
                .willReturn(WireMock.okJson(DCReturnModel.tokenInfoResponse).withStatus(200))
        )
        stubFor(
            WireMock.get(
                WireMock.urlMatching(
                    "/dc/type/marchepublic:consultation_0\\?start=0&limit=50&mpconsultation:organization=https://data.ozwillo.com/dc/type/orgfr:Organisation_0/FR/$siret-"))
                .willReturn(WireMock.okJson(DCReturnModel.dcGetAllConsultationResponse).withStatus(200))
        )


        val entity = restTemplate.getForEntity("/api/marche-public/$siret/consultation", String::class.java)
        assertThat(entity.statusCode).isEqualTo(HttpStatus.OK)
        val resourceList = entity.body!!.split("},{")
        assertThat(resourceList.size).isEqualTo(3)
    }

    @Test
    fun `Test rejection of get consultations list for given Org because Org do not exist in dc`() {
        stubFor(
            WireMock.get(WireMock.urlMatching("/dc/type/orgfr:Organisation_0/FR/$siret"))
                .willReturn(aResponse().withStatus(404))
        )

        val entity = restTemplate.getForEntity("/api/marche-public/$siret/consultation", String::class.java)
        assertThat(entity.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
        assertThat(entity.body).isEqualTo("Organisation with siret $siret does not exist")
    }

    @Test
    fun `Test correct get empty consultations list for given Org`() {
        stubFor(
            WireMock.get(WireMock.urlMatching("/dc/type/orgfr:Organisation_0/FR/$siret"))
                .willReturn(WireMock.okJson(DCReturnModel.dcGetOrganizationResponse).withStatus(200))
        )
        stubFor(
            WireMock.post(WireMock.urlMatching("/a/token"))
                .withHeader("Authorization", EqualToPattern("Basic ZGNpbXBvcnRlcjpzZWNyZXQ="))
                .willReturn(WireMock.okJson(DCReturnModel.tokenInfoResponse).withStatus(200))
        )
        stubFor(
            WireMock.get(
                WireMock.urlMatching(
                    "/dc/type/marchepublic:consultation_0\\?start=0&limit=50&mpconsultation:organization=https://data.ozwillo.com/dc/type/orgfr:Organisation_0/FR/$siret-"))
                .willReturn(WireMock.okJson(DCReturnModel.dcGetEmpty).withStatus(200))
        )


        val entity = restTemplate.getForEntity("/api/marche-public/$siret/consultation", String::class.java)
        assertThat(entity.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(entity.body).isEqualTo("[]")
    }

    @Test
    fun `Test correct get the only consultation for given Org`() {
        stubFor(
            WireMock.get(WireMock.urlMatching("/dc/type/orgfr:Organisation_0/FR/$siret"))
                .willReturn(WireMock.okJson(DCReturnModel.dcGetOrganizationResponse).withStatus(200))
        )
        stubFor(
            WireMock.post(WireMock.urlMatching("/a/token"))
                .withHeader("Authorization", EqualToPattern("Basic ZGNpbXBvcnRlcjpzZWNyZXQ="))
                .willReturn(WireMock.okJson(DCReturnModel.tokenInfoResponse).withStatus(200))
        )
        stubFor(
            WireMock.get(
                WireMock.urlMatching(
                    "/dc/type/marchepublic:consultation_0\\?start=0&limit=50&mpconsultation:organization=https://data.ozwillo.com/dc/type/orgfr:Organisation_0/FR/$siret-"))
                .willReturn(WireMock.okJson(DCReturnModel.dcGetTheOnlyConsultationResponse).withStatus(200))
        )


        val entity = restTemplate.getForEntity("/api/marche-public/$siret/consultation", String::class.java)
        assertThat(entity.statusCode).isEqualTo(HttpStatus.OK)
        val resourceList = entity.body!!.split("},{")
        assertThat(resourceList.size).isEqualTo(1)
    }

    @Test
    fun `Test correct creation of a consultation`() {
        stubFor(
            WireMock.get(WireMock.urlMatching("/dc/type/orgfr:Organisation_0/FR/$siret"))
                .willReturn(WireMock.okJson(DCReturnModel.dcGetOrganizationResponse).withStatus(200))
        )
        stubFor(
            WireMock.get(WireMock.urlMatching("/dc/type/marchepublic:consultation_0/FR/$siret/$referenceConsultation"))
                .willReturn(WireMock.aResponse().withStatus(404))
        )
        stubFor(
            WireMock.post(WireMock.urlMatching("/dc/type/marchepublic:consultation_0"))
                .willReturn(WireMock.okJson(DCReturnModel.dcPostConsultationResponse).withStatus(201))
        )

        val consultation = Consultation(
            reference = referenceConsultation,
            objet = "mon marche",
            datePublication = LocalDateTime.now(),
            dateCloture = LocalDateTime.now(),
            finaliteMarche = FinaliteMarcheType.MARCHE,
            typeMarche = TypeMarcheType.PUBLIC,
            typePrestation = TypePrestationType.FOURNITURES,
            departementsPrestation = listOf(6, 83),
            passation = "passation",
            informatique = true,
            passe = "motdepasse",
            emails = listOf("dev@sictiam.fr", "demat@sictiam.fr"),
            enLigne = false,
            alloti = false,
            invisible = false,
            nbLots = 1
        )

        val entity = restTemplate.postForEntity<String>("/api/marche-public/$siret/consultation", consultation)
        assertThat(entity.statusCode).isEqualTo(HttpStatus.CREATED)
        assertThat(entity.headers["Location"]).isEqualTo(
            listOf("http://localhost:3000/api/marche-public/$siret/consultation/$referenceConsultation"))

        WireMock.verify(WireMock.postRequestedFor(WireMock.urlEqualTo("/dc/type/marchepublic:consultation_0")))
    }

    @Test
    fun `Test rejection of creation of consultation because uri already exist`() {
        stubFor(
            WireMock.get(WireMock.urlMatching("/dc/type/orgfr:Organisation_0/FR/$siret"))
                .willReturn(WireMock.okJson(DCReturnModel.dcGetOrganizationResponse).withStatus(200))
        )
        stubFor(
            WireMock.get(WireMock.urlMatching("/dc/type/marchepublic:consultation_0/FR/$siret/$referenceConsultation"))
                .willReturn(WireMock.okJson(DCReturnModel.dcExistingResponse).withStatus(200))
        )
        stubFor(
            WireMock.post(WireMock.urlMatching("/dc/type/marchepublic:consultation_0"))
                .willReturn(WireMock.okJson(DCReturnModel.dcPostConsultationResponse).withStatus(400))
        )

        val consultation = Consultation(
            reference = referenceConsultation,
            objet = "mon marche",
            datePublication = LocalDateTime.now(),
            dateCloture = LocalDateTime.now(),
            finaliteMarche = FinaliteMarcheType.MARCHE,
            typeMarche = TypeMarcheType.PUBLIC,
            typePrestation = TypePrestationType.FOURNITURES,
            departementsPrestation = listOf(6, 83),
            passation = "passation",
            informatique = true,
            passe = "motdepasse",
            emails = listOf("dev@sictiam.fr", "demat@sictiam.fr"),
            enLigne = false,
            alloti = false,
            invisible = false,
            nbLots = 1
        )

        val entity = restTemplate.postForEntity<String>("/api/marche-public/$siret/consultation", consultation)
        assertThat(entity.statusCode).isEqualTo(HttpStatus.CONFLICT)
        assertThat(entity.headers["Location"]).isNull()

        WireMock.verify(WireMock.postRequestedFor(WireMock.urlEqualTo("/dc/type/marchepublic:consultation_0")))
    }

    @Test
    fun `Test bad request sent to the Datacore`() {
        stubFor(
            WireMock.get(WireMock.urlMatching("/dc/type/orgfr:Organisation_0/FR/$siret"))
                .willReturn(WireMock.okJson(DCReturnModel.dcGetOrganizationResponse).withStatus(200))
        )
        stubFor(
            WireMock.get(WireMock.urlMatching("/dc/type/marchepublic:consultation_0/FR/$siret/$referenceConsultation"))
                .willReturn(WireMock.aResponse().withStatus(404))
        )
        stubFor(
            WireMock.post(WireMock.urlMatching("/dc/type/marchepublic:consultation_0"))
                .willReturn(WireMock.okJson(DCReturnModel.dcPostConsultationResponse).withStatus(400))
        )

        val consultation = Consultation(
            reference = referenceConsultation,
            objet = "mon marche",
            datePublication = LocalDateTime.now(),
            dateCloture = LocalDateTime.now(),
            finaliteMarche = FinaliteMarcheType.MARCHE,
            typeMarche = TypeMarcheType.PUBLIC,
            typePrestation = TypePrestationType.FOURNITURES,
            departementsPrestation = listOf(6, 83),
            passation = "passation",
            informatique = true,
            passe = "motdepasse",
            emails = listOf("dev@sictiam.fr", "demat@sictiam.fr"),
            enLigne = false,
            alloti = false,
            invisible = false,
            nbLots = 1
        )

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
        stubFor(
            WireMock.get(WireMock.urlMatching("/dc/type/orgfr:Organisation_0/FR/$siret"))
                .willReturn(WireMock.okJson(DCReturnModel.dcGetOrganizationResponse).withStatus(200))
        )

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        val entity = HttpEntity(consultationJson, headers)

        val response =
            restTemplate.exchange("/api/marche-public/$siret/consultation", HttpMethod.POST, entity, String::class.java)
        assertThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
        assertThat(response.body).contains("JSON decoding error")
    }

    @Test
    fun `Test correct update of a consultation`() {

        stubFor(
            WireMock.get(WireMock.urlMatching("/dc/type/orgfr:Organisation_0/FR/$siret"))
                .willReturn(WireMock.okJson(DCReturnModel.dcGetOrganizationResponse).withStatus(200))
        )
        stubFor(
            WireMock.post(WireMock.urlMatching("/dc/type/marchepublic:consultation_0"))
                .willReturn(WireMock.okJson(DCReturnModel.dcPostConsultationResponse).withStatus(201))
        )
        stubFor(
            WireMock.get(WireMock.urlMatching("/dc/type/marchepublic:consultation_0/FR/$siret/$referenceConsultation"))
                .willReturn(WireMock.okJson(DCReturnModel.dcExistingResponse).withStatus(200))
        )
        stubFor(
            WireMock.put(WireMock.urlMatching("/dc/type/marchepublic:consultation_0"))
                .willReturn(WireMock.okJson(DCReturnModel.dcPostConsultationResponse).withStatus(200))
        )

        val consultation = Consultation(
            reference = referenceConsultation,
            objet = "mon marche",
            datePublication = LocalDateTime.now(),
            dateCloture = LocalDateTime.now(),
            finaliteMarche = FinaliteMarcheType.MARCHE,
            typeMarche = TypeMarcheType.PUBLIC,
            typePrestation = TypePrestationType.FOURNITURES,
            departementsPrestation = listOf(6, 83),
            passation = "passation",
            informatique = true,
            passe = "motdepasse",
            emails = listOf("dev@sictiam.fr", "demat@sictiam.fr"),
            enLigne = false,
            alloti = false,
            invisible = false,
            nbLots = 1
        )

        val entity = restTemplate.postForEntity<String>("/api/marche-public/$siret/consultation", consultation)
        assertThat(entity.statusCode).isEqualTo(HttpStatus.CREATED)
        assertThat(entity.headers["Location"]).isEqualTo(
            listOf("http://localhost:3000/api/marche-public/$siret/consultation/$referenceConsultation"))

        val updatedConsultation = consultation.copy(objet = "mon nouvel objet", typeMarche = TypeMarcheType.PRIVE)
        val httpEntity = HttpEntity(updatedConsultation)
        val updatedEntity = restTemplate.exchange(
            "/api/marche-public/$siret/consultation/$referenceConsultation",
            HttpMethod.PUT, httpEntity, String::class.java
        )
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
        stubFor(
            WireMock.get(WireMock.urlMatching("/dc/type/orgfr:Organisation_0/FR/$siret"))
                .willReturn(WireMock.okJson(DCReturnModel.dcGetOrganizationResponse).withStatus(200))
        )
        stubFor(
            WireMock.post(WireMock.urlMatching("/dc/type/marchepublic:consultation_0"))
                .willReturn(WireMock.okJson(DCReturnModel.dcPostConsultationResponse).withStatus(201))
        )
        stubFor(
            WireMock.get(WireMock.urlMatching("/dc/type/marchepublic:consultation_0/FR/$siret/$referenceConsultation"))
                .willReturn(WireMock.okJson(dcGetResponse).withStatus(200))
        )
        stubFor(
            WireMock.delete(
                WireMock.urlMatching("/dc/type/marchepublic:consultation_0/FR/$siret/$referenceConsultation"))
                .willReturn(WireMock.aResponse().withStatus(204))
        )

        val consultation = Consultation(
            reference = referenceConsultation,
            objet = "mon marche",
            datePublication = LocalDateTime.now(),
            dateCloture = LocalDateTime.now(),
            finaliteMarche = FinaliteMarcheType.MARCHE,
            typeMarche = TypeMarcheType.PUBLIC,
            typePrestation = TypePrestationType.FOURNITURES,
            departementsPrestation = listOf(6, 83),
            passation = "passation",
            informatique = true,
            passe = "motdepasse",
            emails = listOf("dev@sictiam.fr", "demat@sictiam.fr"),
            enLigne = false,
            alloti = false,
            invisible = false,
            nbLots = 1
        )

        restTemplate.postForEntity<String>("/api/marche-public/$siret/consultation", consultation)
        restTemplate.delete("/api/marche-public/$siret/consultation/$referenceConsultation")

        WireMock.verify(WireMock.postRequestedFor(WireMock.urlEqualTo("/dc/type/marchepublic:consultation_0")))
        WireMock.verify(
            WireMock.deleteRequestedFor(
                WireMock.urlEqualTo("/dc/type/marchepublic:consultation_0/FR/$siret/$referenceConsultation")
            )
                .withHeader("If-Match", EqualToPattern("1"))
        )
    }

    @Test
    fun `Test correct publication of a consultation`() {
        stubFor(
            WireMock.get(WireMock.urlMatching("/dc/type/marchepublic:consultation_0/FR/$siret/$referenceConsultation"))
                .willReturn(WireMock.okJson(DCReturnModel.dcExistingResponse).withStatus(200))
        )
        stubFor(
            WireMock.get(WireMock.urlMatching("/dc/type/orgfr:Organisation_0/FR/$siret"))
                .willReturn(WireMock.okJson(DCReturnModel.dcGetOrganizationResponse).withStatus(200))
        )
        stubFor(
            WireMock.post(WireMock.urlMatching("/dc/type/marchepublic:consultation_0"))
                .willReturn(WireMock.okJson(DCReturnModel.dcPostConsultationResponse).withStatus(201))
        )
        stubFor(
            WireMock.get(WireMock.urlMatching("/dc/type/marchepublic:consultation_0/FR/$siret/$referenceConsultation"))
                .willReturn(WireMock.okJson(DCReturnModel.dcExistingResponse).withStatus(200))
        )
        stubFor(
            WireMock.put(WireMock.urlMatching("/dc/type/marchepublic:consultation_0"))
                .willReturn(WireMock.okJson(DCReturnModel.dcPostConsultationResponse).withStatus(200))
        )

        val entity =
            restTemplate.postForEntity<String>("/api/marche-public/$siret/consultation/$referenceConsultation/publish")
        assertThat(entity.statusCode).isEqualTo(HttpStatus.OK)

        WireMock.verify(
            WireMock.getRequestedFor(
                WireMock.urlEqualTo("/dc/type/marchepublic:consultation_0/FR/$siret/$referenceConsultation")))
    }

    @Test
    fun `Test correct creation of a lot`() {

        stubFor(
            WireMock.get(WireMock.urlMatching("/dc/type/orgfr:Organisation_0/FR/$siret"))
                .willReturn(WireMock.okJson(DCReturnModel.dcGetOrganizationResponse).withStatus(200))
        )
        stubFor(
            WireMock.get(WireMock.urlMatching("/dc/type/marchepublic:consultation_0/FR/$siret/$referenceConsultation"))
                .willReturn(WireMock.okJson(DCReturnModel.dcExistingResponse).withStatus(200))
        )
        stubFor(
            WireMock.post(WireMock.urlMatching("/dc/type/marchepublic:lot_0"))
                .willReturn(WireMock.okJson(DCReturnModel.dcPostLotResponse).withStatus(201))
        )

        val lot = Lot(uuid = uuidLot, libelle = "Lot de test", ordre = 1, numero = 1)
        val entity =
            restTemplate.postForEntity<String>("/api/marche-public/$siret/consultation/$referenceConsultation/lot", lot)
        assertThat(entity.statusCode).isEqualTo(HttpStatus.CREATED)
        assertThat(entity.headers["Location"]).isEqualTo(
            listOf(
                "http://localhost:3000/api/marche-public/$siret/consultation/$referenceConsultation/lot/${lot.uuid}"))

        WireMock.verify(WireMock.postRequestedFor(WireMock.urlEqualTo("/dc/type/marchepublic:lot_0")))
    }

    @Test
    fun `Test bad request sent to datacore during creation of a lot`() {

        stubFor(
            WireMock.get(WireMock.urlMatching("/dc/type/orgfr:Organisation_0/FR/$siret"))
                .willReturn(WireMock.okJson(DCReturnModel.dcGetOrganizationResponse).withStatus(200))
        )
        stubFor(
            WireMock.get(WireMock.urlMatching("/dc/type/marchepublic:consultation_0/FR/$siret/$referenceConsultation"))
                .willReturn(WireMock.okJson(DCReturnModel.dcExistingResponse).withStatus(200))
        )
        stubFor(
            WireMock.post(WireMock.urlMatching("/dc/type/marchepublic:lot_0"))
                .willReturn(WireMock.okJson(DCReturnModel.dcPostLotResponse).withStatus(400))
        )

        val lot = Lot(uuid = uuidLot, libelle = "Lot de test", ordre = 1, numero = 1)
        val entity =
            restTemplate.postForEntity<String>("/api/marche-public/$siret/consultation/$referenceConsultation/lot", lot)
        assertThat(entity.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
        assertThat(entity.headers["Location"]).isNull()

        WireMock.verify(WireMock.postRequestedFor(WireMock.urlEqualTo("/dc/type/marchepublic:lot_0")))
    }

    @Test
    fun `Test correct update of a lot`() {
        stubFor(
            WireMock.get(WireMock.urlMatching("/dc/type/orgfr:Organisation_0/FR/$siret"))
                .willReturn(WireMock.okJson(DCReturnModel.dcGetOrganizationResponse).withStatus(200))
        )
        stubFor(
            WireMock.get(WireMock.urlMatching("/dc/type/marchepublic:consultation_0/FR/$siret/$referenceConsultation"))
                .willReturn(WireMock.okJson(DCReturnModel.dcExistingResponse).withStatus(200))
        )
        stubFor(
            WireMock.post(WireMock.urlMatching("/dc/type/marchepublic:lot_0"))
                .willReturn(WireMock.okJson(DCReturnModel.dcPostLotResponse).withStatus(201))
        )
        stubFor(
            WireMock.get(WireMock.urlMatching("/dc/type/marchepublic:lot_0/FR/$siret/$referenceConsultation/$uuidLot"))
                .willReturn(WireMock.okJson(DCReturnModel.dcExistingLotResponse).withStatus(200))
        )
        stubFor(
            WireMock.put(WireMock.urlMatching("/dc/type/marchepublic:lot_0"))
                .willReturn(WireMock.okJson(DCReturnModel.dcPostLotResponse).withStatus(200))
        )

        val lot = Lot(uuid = uuidLot, libelle = "Lot de test", ordre = 1, numero = 1)

        val entity =
            restTemplate.postForEntity<String>("/api/marche-public/$siret/consultation/$referenceConsultation/lot", lot)
        assertThat(entity.statusCode).isEqualTo(HttpStatus.CREATED)
        assertThat(entity.headers["Location"]).isEqualTo(
            listOf(
                "http://localhost:3000/api/marche-public/$siret/consultation/$referenceConsultation/lot/${lot.uuid}"))

        val updatedLot = lot.copy(libelle = "mon nouveau libellé")
        val httpEntity = HttpEntity(updatedLot)
        val updatedEntity = restTemplate.exchange(
            "/api/marche-public/$siret/consultation/$referenceConsultation/lot/$uuidLot",
            HttpMethod.PUT, httpEntity, String::class.java
        )
        assertThat(updatedEntity.statusCode).isEqualTo(HttpStatus.OK)

        WireMock.verify(WireMock.postRequestedFor(WireMock.urlEqualTo("/dc/type/marchepublic:lot_0")))
        WireMock.verify(WireMock.putRequestedFor(WireMock.urlEqualTo("/dc/type/marchepublic:lot_0")))

    }

    @Test
    fun `Test correct delete of a lot`() {

        val dcGetResponse = """
            {
                "@id" : "http://data.ozwillo.com/dc/type/marchepublic:lot_0/FR/$siret/$referenceConsultation/$uuidLot",
                "o:version" : 1
            }
            """

        stubFor(
            WireMock.get(WireMock.urlMatching("/dc/type/orgfr:Organisation_0/FR/$siret"))
                .willReturn(WireMock.okJson(DCReturnModel.dcGetOrganizationResponse).withStatus(200))
        )
        stubFor(
            WireMock.get(WireMock.urlMatching("/dc/type/marchepublic:consultation_0/FR/$siret/$referenceConsultation"))
                .willReturn(WireMock.okJson(DCReturnModel.dcExistingResponse).withStatus(200))
        )
        stubFor(
            WireMock.post(WireMock.urlMatching("/dc/type/marchepublic:lot_0"))
                .willReturn(WireMock.okJson(DCReturnModel.dcPostLotResponse).withStatus(201))
        )
        stubFor(
            WireMock.get(WireMock.urlMatching("/dc/type/marchepublic:lot_0/FR/$siret/$referenceConsultation/$uuidLot"))
                .willReturn(WireMock.okJson(dcGetResponse).withStatus(200))
        )
        stubFor(
            WireMock.delete(
                WireMock.urlMatching("/dc/type/marchepublic:lot_0/FR/$siret/$referenceConsultation/$uuidLot"))
                .willReturn(WireMock.aResponse().withStatus(204))
        )

        val lot = Lot(uuid = uuidLot, libelle = "Lot de test", ordre = 1, numero = 1)
        restTemplate.postForEntity<String>("/api/marche-public/$siret/consultation/$referenceConsultation/lot", lot)
        restTemplate.delete("/api/marche-public/$siret/consultation/$referenceConsultation/lot/$uuidLot")


        WireMock.verify(WireMock.postRequestedFor(WireMock.urlEqualTo("/dc/type/marchepublic:lot_0")))
        WireMock.verify(
            WireMock.deleteRequestedFor(
                WireMock.urlEqualTo("/dc/type/marchepublic:lot_0/FR/$siret/$referenceConsultation/$uuidLot")
            )
                .withHeader("If-Match", EqualToPattern("1"))
        )
    }

    @Test
    fun `get response register list from datacore`() {

        stubFor(
            WireMock.get(WireMock.urlMatching("/dc/type/marchepublic:consultation_0/FR/$siret/$referenceConsultation"))
                .willReturn(WireMock.okJson(DCReturnModel.dcExistingResponse).withStatus(200))
        )
        stubFor(
            WireMock.post(WireMock.urlMatching("/a/token"))
                .withHeader("Authorization", EqualToPattern("Basic ZGNpbXBvcnRlcjpzZWNyZXQ="))
                .willReturn(WireMock.okJson(DCReturnModel.tokenInfoResponse).withStatus(200))
        )
        stubFor(
            WireMock.get(
                WireMock.urlMatching(
                    "/dc/type/marchepublic:reponse_0\\?start=0&limit=50&mpreponse:consultation=http://data.ozwillo.com/dc/type/marchepublic:consultation_0/FR/$siret/$referenceConsultation-"))
                .willReturn(WireMock.okJson(DCReturnModel.dcGetAllRegisterResponse).withStatus(200))
        )


        val entity =
            restTemplate.getForEntity<String>(
                "/api/marche-public/$siret/registre/$referenceConsultation/${MSUtils.REPONSE_TYPE}")
        assertThat(entity.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(entity.body).contains(cleRegistre)

        WireMock.verify(
            WireMock.getRequestedFor(
                WireMock.urlMatching(
                    "/dc/type/marchepublic:reponse_0\\?start=0&limit=50&mpreponse:consultation=http://data.ozwillo.com/dc/type/marchepublic:consultation_0/FR/$siret/$referenceConsultation-")))

    }

    @Test
    fun `get register empty list from datacore`() {

        stubFor(
            WireMock.get(WireMock.urlMatching("/dc/type/marchepublic:consultation_0/FR/$siret/$referenceConsultation"))
                .willReturn(WireMock.okJson(DCReturnModel.dcExistingResponse).withStatus(200))
        )
        stubFor(
            WireMock.post(WireMock.urlMatching("/a/token"))
                .withHeader("Authorization", EqualToPattern("Basic ZGNpbXBvcnRlcjpzZWNyZXQ="))
                .willReturn(WireMock.okJson(DCReturnModel.tokenInfoResponse).withStatus(200))
        )
        stubFor(
            WireMock.get(
                WireMock.urlMatching(
                    "/dc/type/marchepublic:reponse_0\\?start=0&limit=50&mpreponse:consultation=http://data.ozwillo.com/dc/type/marchepublic:consultation_0/FR/$siret/$referenceConsultation-"))
                .willReturn(WireMock.okJson(DCReturnModel.dcGetEmpty).withStatus(200))
        )


        val entity =
            restTemplate.getForEntity<String>(
                "/api/marche-public/$siret/registre/$referenceConsultation/${MSUtils.REPONSE_TYPE}")
        assertThat(entity.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(entity.body).isEqualTo("[]")

        WireMock.verify(
            WireMock.getRequestedFor(
                WireMock.urlMatching(
                    "/dc/type/marchepublic:reponse_0\\?start=0&limit=50&mpreponse:consultation=http://data.ozwillo.com/dc/type/marchepublic:consultation_0/FR/$siret/$referenceConsultation-")))

    }

    @Test
    fun `get retrait register list from datacore`() {

        stubFor(
            WireMock.get(WireMock.urlMatching("/dc/type/marchepublic:consultation_0/FR/$siret/$referenceConsultation"))
                .willReturn(WireMock.okJson(DCReturnModel.dcExistingResponse).withStatus(200))
        )
        stubFor(
            WireMock.post(WireMock.urlMatching("/a/token"))
                .withHeader("Authorization", EqualToPattern("Basic ZGNpbXBvcnRlcjpzZWNyZXQ="))
                .willReturn(WireMock.okJson(DCReturnModel.tokenInfoResponse).withStatus(200))
        )
        stubFor(
            WireMock.get(
                WireMock.urlMatching(
                    "/dc/type/marchepublic:retrait_0\\?start=0&limit=50&mpretrait:consultation=http://data.ozwillo.com/dc/type/marchepublic:consultation_0/FR/$siret/$referenceConsultation-"))
                .willReturn(WireMock.okJson(DCReturnModel.dcGetAllRegisterRetrait).withStatus(200))
        )
        stubFor(
            WireMock.get(WireMock.urlMatching("/dc/type/marchepublic:personne_0/$clePersonne"))
                .willReturn(WireMock.okJson(DCReturnModel.dcGetPersonResponse).withStatus(200))
        )
        stubFor(
            WireMock.get(WireMock.urlMatching("/dc/type/orgfr:Organisation_0/FR/987654321"))
                .willReturn(WireMock.okJson(DCReturnModel.dcGetOrganizationForRegistre).withStatus(200))
        )

        val entity =
            restTemplate.getForEntity<String>(
                "/api/marche-public/$siret/registre/$referenceConsultation/${MSUtils.RETRAIT_TYPE}")
        assertThat(entity.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(entity.body).contains(referenceConsultation)

        WireMock.verify(
            WireMock.getRequestedFor(
                WireMock.urlMatching(
                    "/dc/type/marchepublic:retrait_0\\?start=0&limit=50&mpretrait:consultation=http://data.ozwillo.com/dc/type/marchepublic:consultation_0/FR/$siret/$referenceConsultation-")))

    }

    @Test
    fun `get retrait register empty list from datacore`() {

        stubFor(
            WireMock.get(WireMock.urlMatching("/dc/type/marchepublic:consultation_0/FR/$siret/$referenceConsultation"))
                .willReturn(WireMock.okJson(DCReturnModel.dcExistingResponse).withStatus(200))
        )
        stubFor(
            WireMock.post(WireMock.urlMatching("/a/token"))
                .withHeader("Authorization", EqualToPattern("Basic ZGNpbXBvcnRlcjpzZWNyZXQ="))
                .willReturn(WireMock.okJson(DCReturnModel.tokenInfoResponse).withStatus(200))
        )
        stubFor(
            WireMock.get(
                WireMock.urlMatching(
                    "/dc/type/marchepublic:retrait_0\\?start=0&limit=50&mpretrait:consultation=http://data.ozwillo.com/dc/type/marchepublic:consultation_0/FR/$siret/$referenceConsultation-"))
                .willReturn(WireMock.okJson(DCReturnModel.dcGetEmpty).withStatus(200))
        )


        val entity =
            restTemplate.getForEntity<String>(
                "/api/marche-public/$siret/registre/$referenceConsultation/${MSUtils.RETRAIT_TYPE}")
        assertThat(entity.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(entity.body).isEqualTo("[]")

        WireMock.verify(
            WireMock.getRequestedFor(
                WireMock.urlMatching(
                    "/dc/type/marchepublic:retrait_0\\?start=0&limit=50&mpretrait:consultation=http://data.ozwillo.com/dc/type/marchepublic:consultation_0/FR/$siret/$referenceConsultation-")))

    }

    @Test
    fun `get retraits resume register list from datacore`() {
        stubFor(
            WireMock.get(WireMock.urlMatching("/dc/type/marchepublic:consultation_0/FR/$siret/$referenceConsultation"))
                .willReturn(WireMock.okJson(DCReturnModel.dcExistingResponse).withStatus(200))
        )
        stubFor(
            WireMock.post(WireMock.urlMatching("/a/token"))
                .withHeader("Authorization", EqualToPattern("Basic ZGNpbXBvcnRlcjpzZWNyZXQ="))
                .willReturn(WireMock.okJson(DCReturnModel.tokenInfoResponse).withStatus(200))
        )
        stubFor(
            WireMock.get(
                WireMock.urlMatching(
                    "/dc/type/marchepublic:retrait_0\\?start=0&limit=50&mpretrait:consultation=http://data.ozwillo.com/dc/type/marchepublic:consultation_0/FR/$siret/$referenceConsultation-"))
                .willReturn(WireMock.okJson(DCReturnModel.dcGetAllRegisterRetrait).withStatus(200))
        )
        stubFor(
            WireMock.get(WireMock.urlMatching("/dc/type/orgfr:Organisation_0/FR/987654321"))
                .willReturn(WireMock.okJson(DCReturnModel.dcGetOrganizationForRegistre).withStatus(200))
        )
        stubFor(
            WireMock.get(WireMock.urlMatching("/dc/type/marchepublic:personne_0/$clePersonne"))
                .willReturn(WireMock.okJson(DCReturnModel.dcGetPersonResponse).withStatus(200))
        )

        val entity =
            restTemplate.getForEntity<String>(
                "/api/marche-public/$siret/registre/$referenceConsultation/retrait/resume")
        assertThat(entity.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(entity.body).contains(referenceConsultation)
    }

    @Test
    fun `get retraits resume register empty list from datacore`() {
        stubFor(
            WireMock.get(WireMock.urlMatching("/dc/type/marchepublic:consultation_0/FR/$siret/$referenceConsultation"))
                .willReturn(WireMock.okJson(DCReturnModel.dcExistingResponse).withStatus(200))
        )
        stubFor(
            WireMock.post(WireMock.urlMatching("/a/token"))
                .withHeader("Authorization", EqualToPattern("Basic ZGNpbXBvcnRlcjpzZWNyZXQ="))
                .willReturn(WireMock.okJson(DCReturnModel.tokenInfoResponse).withStatus(200))
        )
        stubFor(
            WireMock.get(
                WireMock.urlMatching(
                    "/dc/type/marchepublic:retrait_0\\?start=0&limit=50&mpretrait:consultation=http://data.ozwillo.com/dc/type/marchepublic:consultation_0/FR/$siret/$referenceConsultation-"))
                .willReturn(WireMock.okJson(DCReturnModel.dcGetEmpty).withStatus(200))
        )

        val entity =
            restTemplate.getForEntity<String>(
                "/api/marche-public/$siret/registre/$referenceConsultation/retrait/resume")
        assertThat(entity.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(entity.body).isEqualTo(
            "{\"consultationUri\":\"http://data.ozwillo.com/dc/type/marchepublic:consultation_0/FR/$siret/$referenceConsultation\",\"nbreOrg\":0,\"retraitsResume\":[]}")
    }


}
