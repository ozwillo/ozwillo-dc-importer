package org.ozwillo.dcimporter.service

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.matching.EqualToPattern
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.ozwillo.dcimporter.config.DatacoreProperties
import org.ozwillo.dcimporter.model.marchepublic.Consultation
import org.ozwillo.dcimporter.model.marchepublic.FinaliteMarcheType
import org.ozwillo.dcimporter.model.marchepublic.TypeMarcheType
import org.ozwillo.dcimporter.model.marchepublic.TypePrestationType
import org.ozwillo.dcimporter.web.MarchePublicHandler
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.client.getForEntity
import org.springframework.http.HttpStatus
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.LocalDateTime

@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class DatacoreServiceTest(@Autowired val datacoreProperties: DatacoreProperties,
                          @Autowired val restTemplate: TestRestTemplate,
                          @Autowired val datacoreService: DatacoreService) {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(MarchePublicHandler::class.java)

        private val MP_PROJECT = "marchepublic_0"
        private val ORG_TYPE = "orgfr:Organisation_0"
        private val CONSULTATION_TYPE = "marchepublic:consultation_0"
        private val LOT_TYPE = "marchepublic:lot_0"
        private val PIECE_TYPE = "marchepublic:piece_0"
    }

    private lateinit var wireMockServer: WireMockServer

    private val siret = "123456789"

    private val tokenInfoResponse = """
        {
            "active": "true"
        }
        """

    private val bearer = "eyJpZCI6ImFiZDUyY2Y5LTgyYjQtNDJiOC1iZGJmLTA5NmJlNTQyZTEyZC9pdHNmWTFxZWxUR3pScWFEZTkxR3lRIiwiaWF0IjoxNTMxMzE4MDAwLjcyMDAwMDAwMCwiZXhwIjoxNTMxMzIxNjAwLjcyMDAwMDAwMH0"

    @BeforeAll
    fun beforeAll() {
        wireMockServer = WireMockServer(WireMockConfiguration.wireMockConfig().port(8089))
        wireMockServer.start()

        // we need a fake bearer to go through the verification chain
        restTemplate.restTemplate.interceptors.add(ClientHttpRequestInterceptor { request, body, execution ->
            request.headers["Authorization"] = "Bearer eyJpZCI6IjNlMjgwMTc0LWU2NmItNGY2Ny1hZjc0LTZlMDMxYjFiMzllZi8wd3lTSTUtQmNsZTJqVWJNVlNXR2VnIiwiaWF0IjoxNTMxMjE0Mjc5Ljk1ODAwMDAwMCwiZXhwIjoxNTMxMjE3ODc5Ljk1ODAwMDAwMH0"
            execution.execute(request, body)
        })

        WireMock.configureFor(8089)
        WireMock.stubFor(WireMock.post(WireMock.urlMatching("/a/tokeninfo"))
                .withHeader("Authorization", EqualToPattern("Basic ZGNpbXBvcnRlcjpNa2xMcm94V1ZGKy9QRFNqazlONkcra29VZTV5T0ZhL1JodEhmVzg5YzZF"))
                .willReturn(WireMock.okJson(tokenInfoResponse).withStatus(200)))
    }

    @AfterAll
    fun afterAll() {
        wireMockServer.stop()
    }

    @Test
    fun saveResourceTest() {
        val reference = "ref-consultation"
        val date = LocalDateTime.now()
        val consultation = Consultation(reference = "ref-consultation-$date",
                objet = "mon marche", datePublication = LocalDateTime.now(), dateCloture = LocalDateTime.now(),
                finaliteMarche = FinaliteMarcheType.MARCHE, typeMarche = TypeMarcheType.PUBLIC,
                typePrestation = TypePrestationType.FOURNITURES, departementsPrestation = listOf(6, 83),
                passation = "passation", informatique = true, passe = "motdepasse", emails = listOf("dev@sictiam.fr", "demat@sictiam.fr"),
                enLigne = false, alloti = false, invisible = false, nbLots = 1)
        val dcConsultation = consultation.toDcObject(datacoreProperties.baseUri, siret)

        datacoreService.saveResource(MP_PROJECT, CONSULTATION_TYPE, dcConsultation, bearer)
    }
}