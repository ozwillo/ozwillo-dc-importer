package org.ozwillo.dcimporter.service

import org.ozwillo.dcimporter.config.FullLoggingInterceptor
import org.ozwillo.dcimporter.config.KernelProperties
import org.slf4j.LoggerFactory
import org.springframework.http.HttpMethod
import org.springframework.http.RequestEntity
import org.springframework.stereotype.Service
import org.springframework.util.Base64Utils
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestTemplate
import java.net.URI

@Service
class KernelService(private val kernelProperties: KernelProperties) {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(KernelService::class.java)
    }

    fun checkAuthorization(authorizationHeader: List<String>): Boolean {
        if (authorizationHeader.isEmpty() || authorizationHeader.size > 1)
            return false

        val bearerValue = authorizationHeader[0].split(" ")[1]
        if (bearerValue.isEmpty())
            return false

        val restTemplate = RestTemplate()
        restTemplate.interceptors.add(FullLoggingInterceptor())
        val headers = LinkedMultiValueMap<String, String>()
        val idAndSecret = kernelProperties.clientId + ":" + kernelProperties.clientSecret
        headers.set("Authorization", "Basic ${String(Base64Utils.encode(idAndSecret.toByteArray()))}")
        headers.set("Content-Type", "application/x-www-form-urlencoded")
        val uri = "${kernelProperties.accountUri}/a/tokeninfo"
        val request = RequestEntity("token=$bearerValue", headers, HttpMethod.POST, URI(uri))
        val response = restTemplate.exchange(request, TokenInfoResponse::class.java)

        return response.body?.active ?: kotlin.run { false }
    }

    data class TokenInfoResponse(val active: Boolean)
}