package org.ozwillo.dcimporter.handler

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.ozwillo.dcimporter.model.marchepublic.Consultation
import org.ozwillo.dcimporter.model.marchepublic.FinaliteMarcheType
import org.ozwillo.dcimporter.model.marchepublic.TypeMarcheType
import org.ozwillo.dcimporter.model.marchepublic.TypePrestationType
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.client.postForEntity
import org.springframework.http.*
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.LocalDateTime


@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MarchePublicHandlerTest(@Autowired val restTemplate: TestRestTemplate) {

    private lateinit var wireMockServer: WireMockServer

    @BeforeAll
    fun beforeAll() {
        wireMockServer = WireMockServer(wireMockConfig().port(8089))
        wireMockServer.start()
    }

    @AfterEach
    fun afterEach() {
        wireMockServer.resetAll()
    }

    @AfterAll
    fun afterAll() {
        wireMockServer.stop()
    }

    @Test
    fun `Test correct creation of a consultation`() {
        val dcResponse = """
            {
                "@id": "http://data.ozwillo.com/dc/type/marchepublic:consultation_0/123456/ref-interne"
            }
            """
        WireMock.configureFor(8089)
        WireMock.stubFor(WireMock.post(WireMock.urlMatching("/dc/type/marchepublic:consultation_0"))
                .willReturn(WireMock.okJson(dcResponse).withStatus(201)))

        val consultation = Consultation(idPouvoirAdjudicateur = "123456", reference = "ref-consultation",
                objet = "mon marche", datePublication = LocalDateTime.now(), dateCloture = LocalDateTime.now(),
                refInterne = "ref-interne", finaliteMarche = FinaliteMarcheType.MARCHE, typeMarche = TypeMarcheType.PUBLIQUE,
                typePrestation = TypePrestationType.FOURNITURES, departementsPrestation = listOf(6, 83),
                passation = "passation", informatique = true, passe = "motdepasse", emails = listOf("dev@sictiam.fr", "demat@sictiam.fr"),
                enLigne = false, alloti = false, invisible = false, nbLots = 1)

        val entity = restTemplate.postForEntity<String>("/api/marche-public/consultation", consultation)
        assertThat(entity.statusCode).isEqualTo(HttpStatus.CREATED)
        assertThat(entity.headers["Location"]).isEqualTo(listOf("http://data.ozwillo.com/dc/type/marchepublic:consultation_0/123456/ref-interne"))

        WireMock.verify(WireMock.postRequestedFor(WireMock.urlEqualTo("/dc/type/marchepublic:consultation_0")))
    }

    @Test
    fun `Test bad request sent to the Datacore`() {
        WireMock.configureFor(8089)
        WireMock.stubFor(WireMock.post(WireMock.urlMatching("/dc/type/marchepublic:consultation_0"))
                .willReturn(WireMock.aResponse().withStatus(400)))

        val consultation = Consultation(idPouvoirAdjudicateur = "123456", reference = "ref-consultation",
                objet = "mon marche", datePublication = LocalDateTime.now(), dateCloture = LocalDateTime.now(),
                refInterne = "ref-interne", finaliteMarche = FinaliteMarcheType.MARCHE, typeMarche = TypeMarcheType.PUBLIQUE,
                typePrestation = TypePrestationType.FOURNITURES, departementsPrestation = listOf(6, 83),
                passation = "passation", informatique = true, passe = "motdepasse", emails = listOf("dev@sictiam.fr", "demat@sictiam.fr"),
                enLigne = false, alloti = false, invisible = false, nbLots = 1)

        val entity = restTemplate.postForEntity<String>("/api/marche-public/consultation", consultation)
        assertThat(entity.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
        assertThat(entity.headers["Location"]).isNull()

        WireMock.verify(WireMock.postRequestedFor(WireMock.urlEqualTo("/dc/type/marchepublic:consultation_0")))
    }

    @Test
    fun `Test illformed consultation payload`() {
        val consultationJson = """
            {
                "idPouvoirAdjudicateur": "123456",
                "reference": "reference",
                "objet": "mon marché public",
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
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        val entity = HttpEntity(consultationJson, headers)

        val response = restTemplate.exchange("/api/marche-public/consultation", HttpMethod.POST, entity, String::class.java)
        assertThat(response.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
        assertThat(response.body).startsWith("JSON decoding error")
    }
}