package org.ozwillo.dcimporter.handler

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.web.reactive.function.client.WebClient
import reactor.test.test

@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
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
