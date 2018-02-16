package org.ozwillo.dcimporter.web

import org.ozwillo.dcimporter.model.publik.FormModel
import org.ozwillo.dcimporter.service.PublikService
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters.empty
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.bodyToMono
import reactor.core.publisher.Mono

@Component
class PublikHandler(private val publikService: PublikService) {

    fun publish(req: ServerRequest): Mono<ServerResponse> =
            req.bodyToMono<FormModel>()
                    .flatMap { formModel -> publikService.saveResourceToDC(formModel) }
                    .flatMap { _ -> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(empty<String>())}
}
