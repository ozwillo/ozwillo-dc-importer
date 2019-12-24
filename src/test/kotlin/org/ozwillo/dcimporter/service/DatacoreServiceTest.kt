package org.ozwillo.dcimporter.service

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import reactor.test.StepVerifier

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class DatacoreServiceTest {

    @Autowired
    private lateinit var datacoreService: DatacoreService

    private lateinit var wireMockServer: WireMockServer

    @BeforeAll
    fun beforeAll() {
        wireMockServer = WireMockServer(WireMockConfiguration.wireMockConfig().port(8089))
        wireMockServer.start()

        configureFor(8089)
    }

    @AfterAll
    fun afterAll() {
        wireMockServer.stop()
    }

    @AfterEach
    fun afterEach() {
        reset()
    }

    @Test
    fun `it should call the DC to delete the resource`() {

        val resourceIri = "FR/1234/4567"
        val type = "grant:association_0"
        val getResourceIriResponse = """
            {
                "@id": "http://data.ozwillo.com/dc/type/$type/$resourceIri",
                "o:version": 12
            }
        """.trimIndent()

        stubFor(
            get(urlMatching("/dc/type/$type/$resourceIri"))
                .willReturn(okJson(getResourceIriResponse).withStatus(200))
        )
        stubFor(
            delete(urlEqualTo("/dc/type/$type/$resourceIri"))
                .willReturn(noContent())
        )

        val deleteResult = datacoreService.deleteResource("grant_0", type, resourceIri, "mybearer")

        StepVerifier.create(deleteResult)
            .expectNext(true)
            .expectComplete()
            .verify()

        assertEquals(0, findUnmatchedRequests().size)
        verify(getRequestedFor(urlPathEqualTo("/dc/type/$type/$resourceIri")))
        verify(deleteRequestedFor(urlPathEqualTo("/dc/type/$type/$resourceIri"))
            .withHeader("If-Match", equalTo("12"))
            .withHeader("X-Datacore-Project", equalTo("grant_0"))
            .withHeader("Authorization", equalTo("Bearer mybearer")))
    }

    @Test
    fun `it should throw an error if the resource to be deleted does not exist`() {

        val resourceIri = "FR/1234/4567"
        val type = "grant:association_0"

        stubFor(
            get(urlMatching("/dc/type/$type/$resourceIri"))
                .willReturn(notFound().withStatus(404))
        )

        val deleteResult = datacoreService.deleteResource("grant_0", type, resourceIri, "mybearer")

        StepVerifier.create(deleteResult)
            .expectComplete()
            .verify()

        assertEquals(0, findUnmatchedRequests().size)
        verify(getRequestedFor(urlPathEqualTo("/dc/type/$type/$resourceIri")))
    }
}
