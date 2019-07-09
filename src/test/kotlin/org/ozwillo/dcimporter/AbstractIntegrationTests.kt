package org.ozwillo.dcimporter

import org.junit.jupiter.api.BeforeAll
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.test.context.ActiveProfiles
import org.springframework.web.reactive.function.client.WebClient

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class AbstractIntegrationTests {

    @LocalServerPort
    var port: Int? = null

    // TODO Migrate to WebTestClient when https://youtrack.jetbrains.com/issue/KT-5464 will be fixed
    protected lateinit var client: WebClient

    @BeforeAll
    fun setup() {
        client = WebClient.create("http://localhost:$port")
    }
}
