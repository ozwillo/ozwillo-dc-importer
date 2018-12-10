package org.ozwillo.dcimporter.handler

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension
import wiremock.org.apache.http.HttpResponse
import wiremock.org.apache.http.HttpStatus
import wiremock.org.apache.http.client.methods.HttpGet
import wiremock.org.apache.http.client.methods.HttpUriRequest
import wiremock.org.apache.http.impl.client.HttpClientBuilder

@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class InseeSireneTest {

    @Test
    fun `is Insee Sirene API responding test`() {

        val request: HttpUriRequest = HttpGet("https://api.insee.fr/entreprises/sirene/V3/siret")

        val response: HttpResponse = HttpClientBuilder.create().build().execute(request)

        Assertions.assertThat(response.statusLine.statusCode).isEqualTo(HttpStatus.SC_UNAUTHORIZED)
    }

}