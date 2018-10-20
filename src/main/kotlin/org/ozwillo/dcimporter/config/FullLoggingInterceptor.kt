package org.ozwillo.dcimporter.config

import org.slf4j.LoggerFactory
import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse
import java.io.IOException
import java.nio.charset.Charset

class FullLoggingInterceptor : ClientHttpRequestInterceptor {

    companion object {
        private val logger = LoggerFactory.getLogger(FullLoggingInterceptor::class.java)
    }

    override fun intercept(
        request: HttpRequest,
        body: ByteArray,
        execution: ClientHttpRequestExecution
    ): ClientHttpResponse {

        traceRequest(request, body)
        val response = execution.execute(request, body)
        // traceResponse(response)
        return response
    }

    @Throws(IOException::class)
    private fun traceRequest(request: HttpRequest, body: ByteArray) {
        logger.debug("===========================request begin================================================")
        logger.debug("URI         : {}", request.uri)
        logger.debug("Method      : {}", request.method)
        logger.debug("Headers     : {}", request.headers)
        logger.debug("Request body: {}", String(body, Charset.forName("UTF-8")))
        logger.debug("==========================request end================================================")
    }
}