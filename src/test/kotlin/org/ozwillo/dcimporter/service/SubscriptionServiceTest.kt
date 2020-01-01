package org.ozwillo.dcimporter.service

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.github.tomakehurst.wiremock.matching.AnythingPattern
import java.util.*
import org.junit.jupiter.api.*
import org.ozwillo.dcimporter.model.Subscription
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.exists
import org.springframework.test.context.ActiveProfiles
import reactor.test.StepVerifier

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class SubscriptionServiceTest {

    @Autowired
    private lateinit var subscriptionService: SubscriptionService

    @Autowired
    private lateinit var mongoTemplate: MongoTemplate

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

    @BeforeEach
    fun resetSubscriptionCollection() {
        mongoTemplate.remove(Query(Subscription::url exists true), Subscription::class.java)
    }

    @Test
    fun `it should return one matching subscription`() {

        mongoTemplate.save(Subscription(url = "http://localhost:8080",
            applicationName = "App 1", events = listOf("grant_0.grant:assocation_0.create", "grant_0.grant:assocation_0.delete")))
        mongoTemplate.save(Subscription(url = "http://localhost:8080",
            applicationName = "App 2", events = listOf("grant_0.grant:assocation_0.update")))

        val result = subscriptionService.findForEventType("grant_0.grant:assocation_0.create")

        StepVerifier.create(result)
            .expectNextMatches {
                it.applicationName == "App 1"
            }
            .expectComplete()
            .verify()
    }

    @Test
    fun `it should not return any subscription`() {

        mongoTemplate.save(Subscription(url = "http://localhost:8080",
            applicationName = "App 1", events = listOf("grant_0.grant:assocation_0.create", "grant_0.grant:assocation_0.delete")))
        mongoTemplate.save(Subscription(url = "http://localhost:8080",
            applicationName = "App 2", events = listOf("grant_0.grant:assocation_0.create")))

        val result = subscriptionService.findForEventType("grant_0.grant:assocation_0.update")

        StepVerifier.create(result)
            .expectComplete()
            .verify()
    }

    @Test
    fun `it should return all subscriptions`() {

        mongoTemplate.save(Subscription(url = "http://localhost:8080",
            applicationName = "App 1", events = listOf("grant_0.grant:assocation_0.create")))
        mongoTemplate.save(Subscription(url = "http://localhost:8080",
            applicationName = "App 2", events = listOf("grant_0.grant:assocation_0.update")))

        val result = subscriptionService.findAll()

        StepVerifier.create(result)
            .expectNextMatches {
                it.applicationName == "App 1"
            }
            .expectNextMatches {
                it.applicationName == "App 2"
            }
            .expectComplete()
            .verify()
    }

    @Test
    fun `it should call one notifier and log a successful notification`() {
        val dcResourceJson = """
            {
              "@id": "http://data.ozwillo.com/dc/type/grant:association_0/FR/1234"
            }
        """.trimIndent()
        val subscriptionId = UUID.randomUUID()
        mongoTemplate.save(Subscription(uuid = subscriptionId, url = "http://localhost:8089/notify",
            applicationName = "App 1", events = listOf("grant_0.grant:assocation_0.create")))

        stubFor(
            post(urlMatching("/notify"))
                .willReturn(ok().withStatus(200))
        )

        val result = subscriptionService.notifyForEventType("grant_0.grant:assocation_0.create", dcResourceJson)

        StepVerifier.create(result)
            .expectNextMatches {
                it.eventType == "grant_0.grant:assocation_0.create" &&
                        it.subscriptionId == subscriptionId &&
                        it.errorMessage == null &&
                        it.result == 200
            }
            .expectComplete()
            .verify()

        verify(postRequestedFor(urlPathEqualTo("/notify"))
            .withHeader("X-Ozwillo-Event", equalTo("grant_0.grant:assocation_0.create"))
            .withHeader("X-Ozwillo-Delivery", AnythingPattern())
            .withRequestBody(equalToJson(dcResourceJson)))
    }

    @Test
    fun `it should call one notifier and log an error notification on 4XX errors`() {
        val dcResourceJson = """
            {
              "@id": "http://data.ozwillo.com/dc/type/grant:association_0/FR/1234"
            }
        """.trimIndent()
        val subscriptionId = UUID.randomUUID()
        mongoTemplate.save(Subscription(uuid = subscriptionId, url = "http://localhost:8089/notify",
            applicationName = "App 1", events = listOf("grant_0.grant:assocation_0.create")))

        stubFor(
            post(urlMatching("/notify"))
                .willReturn(forbidden().withStatus(403).withBody("Forbidden"))
        )

        val result = subscriptionService.notifyForEventType("grant_0.grant:assocation_0.create", dcResourceJson)

        StepVerifier.create(result)
            .expectNextMatches {
                it.eventType == "grant_0.grant:assocation_0.create" &&
                        it.subscriptionId == subscriptionId &&
                        it.errorMessage == "Forbidden" &&
                        it.result == 403
            }
            .expectComplete()
            .verify()

        verify(postRequestedFor(urlPathEqualTo("/notify")))
    }

    @Test
    fun `it should call one notifier and log an error notification on 5XX errors`() {
        val dcResourceJson = """
            {
              "@id": "http://data.ozwillo.com/dc/type/grant:association_0/FR/1234"
            }
        """.trimIndent()
        val subscriptionId = UUID.randomUUID()
        mongoTemplate.save(Subscription(uuid = subscriptionId, url = "http://localhost:8089/notify",
            applicationName = "App 1", events = listOf("grant_0.grant:assocation_0.create")))

        stubFor(
            post(urlMatching("/notify"))
                .willReturn(aResponse().withStatus(500).withBody("Internal Server Error"))
        )

        val result = subscriptionService.notifyForEventType("grant_0.grant:assocation_0.create", dcResourceJson)

        StepVerifier.create(result)
            .expectNextMatches {
                it.eventType == "grant_0.grant:assocation_0.create" &&
                        it.subscriptionId == subscriptionId &&
                        it.errorMessage == "Internal Server Error" &&
                        it.result == 500
            }
            .expectComplete()
            .verify()

        verify(postRequestedFor(urlPathEqualTo("/notify")))
    }
}
