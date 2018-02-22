package org.ozwillo.dcimporter.web

import org.ozwillo.dcimporter.model.publik.FormModel
import org.ozwillo.dcimporter.service.PublikService
import org.ozwillo.dcimporter.service.SubscriptionService
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters.fromObject
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.bodyToMono
import reactor.core.publisher.Mono

@Component
class PublikHandler(private val publikService: PublikService,
                    private val subscriptionService: SubscriptionService) {

    fun publish(req: ServerRequest): Mono<ServerResponse> =
            req.bodyToMono<FormModel>()
                    .flatMap { formModel -> publikService.saveResourceToDC(formModel) }
                    .flatMap { dcResult ->
                        subscriptionService.notify(dcResult)
                                .collectList()
                                .flatMap { resultList ->
                                    ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(fromObject(resultList))
                                }
                    }
}
