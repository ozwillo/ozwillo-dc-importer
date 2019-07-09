package org.ozwillo.dcimporter.handler

import org.junit.Assert
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class StatusHandlerTest(@Autowired val webTestClient: WebTestClient) {

    @Test
    fun `Assert status page return code`() {
        webTestClient.get()
            .uri("/api/status")
            .exchange()
            .expectStatus().isOk
            .expectBody().consumeWith { response ->
                Assert.assertEquals(String(response.responseBody!!), "OK")
            }
    }
}
