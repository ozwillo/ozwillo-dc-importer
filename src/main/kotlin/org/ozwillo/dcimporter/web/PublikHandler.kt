package org.ozwillo.dcimporter.web

import org.ozwillo.dcimporter.model.datacore.DCBusinessResourceLight
import org.ozwillo.dcimporter.model.datacore.DCResultSingle
import org.ozwillo.dcimporter.model.publik.FormModel
import org.ozwillo.dcimporter.service.DatacoreService
import org.ozwillo.dcimporter.service.MaarchService
import org.ozwillo.dcimporter.service.PublikService
import org.ozwillo.dcimporter.service.SubscriptionService
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters.fromObject
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.bodyToMono
import reactor.core.publisher.Mono

@Component
class PublikHandler(private val datacoreService: DatacoreService,
                    private val publikService: PublikService,
                    private val maarchService: MaarchService,
                    private val subscriptionService: SubscriptionService) {

    @Value("\${publik.datacore.project}")
    private val datacoreProject: String = "datacoreProject"

    fun publish(req: ServerRequest): Mono<ServerResponse> =
            req.bodyToMono<FormModel>()
                    .map { formModel -> publikService.formToDCResource(formModel) }
                    .flatMap { dcResourceWithProject ->
                        datacoreService.saveResource(datacoreProject, dcResourceWithProject.first, dcResourceWithProject.second)
                                .map { _ -> dcResourceWithProject }
                    }
                    .flatMap { dcResourceWithProject ->
                        maarchService.onNewData(dcResourceWithProject.second)
//                        subscriptionService.notify(dcResourceWithProject.first, dcResourceWithProject.second)
//                                .collectList()
                    }
                    .flatMap { result ->
                        ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).body(fromObject(result))
                    }
                    //}
}
