package org.ozwillo.dcimporter.service

import java.nio.charset.StandardCharsets
import java.util.*
import org.ozwillo.dcimporter.config.DatacoreProperties
import org.ozwillo.dcimporter.config.KernelProperties
import org.ozwillo.dcimporter.model.oauth.TokenResponse
import org.springframework.http.MediaType
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.stereotype.Service
import org.springframework.util.Base64Utils
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import reactor.netty.http.client.HttpClient

@Service
class KernelService(
    private val kernelProperties: KernelProperties,
    private val datacoreProperties: DatacoreProperties
) {

    fun getAccessToken(): Mono<String> {
        val httpClient = HttpClient.create().wiretap(true)
        val client = WebClient.builder().clientConnector(ReactorClientHttpConnector(httpClient)).build() // .create(kernelProperties.tokenEndpoint)
        val authorizationHeaderValue = "Basic " + Base64Utils.encodeToString(
            String.format(Locale.ROOT, "%s:%s", kernelProperties.clientId, kernelProperties.clientSecret)
                .toByteArray(StandardCharsets.UTF_8)
        )

        return client.post()
            .uri(kernelProperties.tokenEndpoint)
            .header("Authorization", authorizationHeaderValue)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(
                BodyInserters.fromFormData("grant_type", "refresh_token")
                    .with("refresh_token", datacoreProperties.systemAdminUser.refreshToken))
            .retrieve()
            .bodyToMono(TokenResponse::class.java)
            .map { it.accessToken }
    }
}
