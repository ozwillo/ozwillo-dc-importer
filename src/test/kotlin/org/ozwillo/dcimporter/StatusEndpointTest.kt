package org.ozwillo.dcimporter

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.test.test

class StatusEndpointTest : AbstractIntegrationTests() {

    @Test
    fun status() {
        client.get().uri("/api/status").exchange().test()
                .consumeNextWith {
                    assertThat(it.statusCode()).isEqualTo(HttpStatus.OK)
                    it.bodyToMono<String>().map {
                        assertThat(it).isEqualTo("OK")
                    }
                }
                .verifyComplete()
    }
}