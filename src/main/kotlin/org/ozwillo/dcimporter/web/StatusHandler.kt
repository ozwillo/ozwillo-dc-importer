package org.ozwillo.dcimporter.web

import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters.fromObject
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono

@Component
class StatusHandler {

    fun status(req: ServerRequest): Mono<ServerResponse> =
        ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(fromObject("OK"))
}
