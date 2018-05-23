package org.ozwillo.dcimporter.web

import org.ozwillo.dcimporter.config.DatacoreProperties
import org.ozwillo.dcimporter.model.datacore.DCBusinessResourceLight
import org.ozwillo.dcimporter.model.datacore.DCResultSingle
import org.ozwillo.dcimporter.model.marchepublic.Consultation
import org.ozwillo.dcimporter.service.DatacoreService
import org.ozwillo.dcimporter.service.KernelService
import org.ozwillo.dcimporter.service.SubscriptionService
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.*
import org.springframework.web.reactive.function.server.bodyToMono
import reactor.core.publisher.Mono
import reactor.util.function.Tuple2
import java.net.URI

@Component
class MarchePublicHandler(private val datacoreProperties: DatacoreProperties,
                          private val kernelService: KernelService,
                          private val datacoreService: DatacoreService,
                          private val subscriptionService: SubscriptionService) {

    fun create(req: ServerRequest): Mono<ServerResponse> {
        if (!kernelService.checkAuthorization(req.headers().header("Authorization")))
            return status(HttpStatus.UNAUTHORIZED).body(BodyInserters.empty<String>())

        val dcConsultation: Mono<DCBusinessResourceLight> = req.bodyToMono<Consultation>()
                .map { consultation -> consultation.toDcObject(datacoreProperties.baseUri) }

        val dcAndNotifyResult: Mono<Tuple2<DCResultSingle, String>> = dcConsultation.flatMap {
            val dcResult: Mono<DCResultSingle> = datacoreService.saveResource("marchepublic_0", "marchepublic:consultation_0", it)
            val notifyResult: Mono<String> = subscriptionService.notifyMock("marchepublic:consultation_0", it)
            dcResult.zipWith(notifyResult)
        }

        return dcAndNotifyResult.flatMap { result ->
            created(URI(result.t1.resource.getUri()))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.empty<String>())
        }.onErrorResume { error ->
            badRequest().body(BodyInserters.fromObject(error.message.orEmpty()))
        }
    }
}