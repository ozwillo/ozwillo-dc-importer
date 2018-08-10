package org.ozwillo.dcimporter.handler

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.stubFor
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import com.github.tomakehurst.wiremock.matching.EqualToPattern
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.ozwillo.dcimporter.config.RabbitMockConfig
import org.ozwillo.dcimporter.model.marchepublic.Consultation
import org.ozwillo.dcimporter.model.marchepublic.FinaliteMarcheType
import org.ozwillo.dcimporter.model.marchepublic.TypeMarcheType
import org.ozwillo.dcimporter.model.marchepublic.TypePrestationType
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.client.postForEntity
import org.springframework.context.annotation.Import
import org.springframework.http.*
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.LocalDateTime

@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(RabbitMockConfig::class)
class MarchePublicHandlerTest(@Autowired val restTemplate: TestRestTemplate) {

    private lateinit var wireMockServer: WireMockServer

    private val siret = "123456789"
    private val referenceConsultation = "ref-consultation"

    private val tokenInfoResponse = """
        {
            "active": "true"
        }
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
    }

    @AfterAll
    fun afterAll() {
        wireMockServer.stop()
    }

    @Test
    fun `Test correct creation of a consultation`() {
        stubFor(WireMock.post(WireMock.urlMatching("/a/tokeninfo"))
                .withHeader("Authorization", EqualToPattern("Basic ZGNpbXBvcnRlcjpNa2xMcm94V1ZGKy9QRFNqazlONkcra29VZTV5T0ZhL1JodEhmVzg5YzZF"))
                .willReturn(WireMock.okJson(tokenInfoResponse).withStatus(200)))
        stubFor(WireMock.get(WireMock.urlMatching("/dc/type/orgfr:Organisation_0/FR/$siret"))
                .willReturn(WireMock.okJson(dcGetOrganizationResponse).withStatus(200)))
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
    fun `Test bad request sent to the Datacore`() {
        stubFor(WireMock.post(WireMock.urlMatching("/a/tokeninfo"))
                .withHeader("Authorization", EqualToPattern("Basic ZGNpbXBvcnRlcjpNa2xMcm94V1ZGKy9QRFNqazlONkcra29VZTV5T0ZhL1JodEhmVzg5YzZF"))
                .willReturn(WireMock.okJson(tokenInfoResponse).withStatus(200)))
        stubFor(WireMock.get(WireMock.urlMatching("/dc/type/orgfr:Organisation_0/FR/$siret"))
                .willReturn(WireMock.okJson(dcGetOrganizationResponse).withStatus(200)))
        stubFor(WireMock.post(WireMock.urlMatching("/dc/type/marchepublic:consultation_0"))
                .willReturn(WireMock.aResponse().withStatus(400)))

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
        stubFor(WireMock.post(WireMock.urlMatching("/a/tokeninfo"))
                .withHeader("Authorization", EqualToPattern("Basic ZGNpbXBvcnRlcjpNa2xMcm94V1ZGKy9QRFNqazlONkcra29VZTV5T0ZhL1JodEhmVzg5YzZF"))
                .willReturn(WireMock.okJson(tokenInfoResponse).withStatus(200)))
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
        val dcExistingResponse = """
            {
                "@id": "http://data.ozwillo.com/dc/type/marchepublic:consultation_0/FR/$siret/$referenceConsultation",
                "o:version": "0"
            }
            """
        stubFor(WireMock.post(WireMock.urlMatching("/a/tokeninfo"))
                .withHeader("Authorization", EqualToPattern("Basic ZGNpbXBvcnRlcjpNa2xMcm94V1ZGKy9QRFNqazlONkcra29VZTV5T0ZhL1JodEhmVzg5YzZF"))
                .willReturn(WireMock.okJson(tokenInfoResponse).withStatus(200)))
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
        stubFor(WireMock.post(WireMock.urlMatching("/a/tokeninfo"))
                .withHeader("Authorization", EqualToPattern("Basic ZGNpbXBvcnRlcjpNa2xMcm94V1ZGKy9QRFNqazlONkcra29VZTV5T0ZhL1JodEhmVzg5YzZF"))
                .willReturn(WireMock.okJson(tokenInfoResponse).withStatus(200)))
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
}