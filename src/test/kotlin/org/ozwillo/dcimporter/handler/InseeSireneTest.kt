package org.ozwillo.dcimporter.handler

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import org.springframework.web.reactive.function.client.WebClient
import reactor.test.test

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class InseeSireneTest {

    @Test
    fun `is Insee Sirene API responding test`() {

        val client = WebClient.create()
        client.get().uri("https://api.insee.fr/entreprises/sirene/V3/siret")
            .exchange()
            .test()
            .consumeNextWith {
                Assertions.assertThat(it.statusCode()).isEqualTo(HttpStatus.UNAUTHORIZED)
            }
            .verifyComplete()
    }

}
