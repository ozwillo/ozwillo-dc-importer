package org.ozwillo.dcimporter.config

import org.ozwillo.dcimporter.service.exceptions.BearerNotFoundException
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.codec.HttpMessageWriter
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.HandlerStrategies
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.result.view.ViewResolver
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebExceptionHandler
import reactor.core.publisher.Mono

@Component
@Order(-2)
class CustomWebExceptionHandler : WebExceptionHandler {
    override fun handle(exchange: ServerWebExchange, ex: Throwable): Mono<Void> {
        if (ex is BearerNotFoundException) {
            return ServerResponse.status(HttpStatus.UNAUTHORIZED)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(ex.toErrorMessage())
                .flatMap {
                    it.writeTo(exchange, HandlerStrategiesResponseContext(HandlerStrategies.withDefaults()))
                }
                .flatMap {
                    Mono.empty<Void>()
                }
        }

        return Mono.error(ex)
    }
}

private class HandlerStrategiesResponseContext(val strategies: HandlerStrategies) : ServerResponse.Context {

    override fun messageWriters(): List<HttpMessageWriter<*>> {
        return this.strategies.messageWriters()
    }

    override fun viewResolvers(): List<ViewResolver> {
        return this.strategies.viewResolvers()
    }
}
