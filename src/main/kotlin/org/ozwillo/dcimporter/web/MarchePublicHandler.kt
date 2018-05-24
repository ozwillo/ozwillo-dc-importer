package org.ozwillo.dcimporter.web

import org.ozwillo.dcimporter.config.ApplicationProperties
import org.ozwillo.dcimporter.config.DatacoreProperties
import org.ozwillo.dcimporter.model.marchepublic.Consultation
import org.ozwillo.dcimporter.service.DatacoreService
import org.ozwillo.dcimporter.service.KernelService
import org.ozwillo.dcimporter.service.SubscriptionService
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.*
import org.springframework.web.reactive.function.server.bodyToMono
import reactor.core.publisher.Mono
import java.net.URI

@Component
class MarchePublicHandler(private val datacoreProperties: DatacoreProperties,
                          private val datacoreService: DatacoreService,
                          private val subscriptionService: SubscriptionService,
                          private val applicationProperties: ApplicationProperties) {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(MarchePublicHandler::class.java)
    }

    fun create(req: ServerRequest): Mono<ServerResponse> {
        val bearer = extractBearer(req.headers())
        // TODO : check existence
        val siret = req.pathVariable("siret")
        return req.bodyToMono<Consultation>()
                .flatMap { consultation ->
                    val dcConsultation = consultation.toDcObject(datacoreProperties.baseUri, siret)
                    datacoreService.saveResource("marchepublic_0", "marchepublic:consultation_0",
                            dcConsultation, bearer)
                }
                .flatMap { result ->
                    val reference = result.resource.getUri().substringAfterLast('/')
                    // val notifyResult: Mono<String> = subscriptionService.notifyMock("marchepublic:consultation_0", it)
                    val resourceUri = "${applicationProperties.url}/api/marche-public/$siret/consultation/$reference"
                    created(URI(resourceUri))
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(BodyInserters.empty<String>())
                }.onErrorResume { error ->
                    // TODO : inspect error message to know if conflict or bad request
                    LOGGER.error("Creation failed with error $error")
                    badRequest().body(BodyInserters.fromObject(error.message.orEmpty()))
                }
    }

    fun update(req: ServerRequest): Mono<ServerResponse> {
        val bearer = extractBearer(req.headers())
        // TODO : check existence
        val siret = req.pathVariable("siret")
        return req.bodyToMono<Consultation>()
                .flatMap { consultation ->
                    val dcConsultation = consultation.toDcObject(datacoreProperties.baseUri, siret, req.pathVariable("reference"))
                    datacoreService.updateResource("marchepublic_0", "marchepublic:consultation_0", dcConsultation, bearer)
                }
                .flatMap { _ ->
                    // val notifyResult: Mono<String> = subscriptionService.notifyMock("marchepublic:consultation_0", it)
                    ok().contentType(MediaType.APPLICATION_JSON)
                        .body(BodyInserters.empty<String>())
                }.onErrorResume { error ->
                    // TODO : inspect error message to know if conflict or bad request
                    badRequest().body(BodyInserters.fromObject(error.message.orEmpty()))
                }
    }

    fun delete(req: ServerRequest): Mono<ServerResponse> {
        val bearer = extractBearer(req.headers())
        val iri = "FR/${req.pathVariable("siret")}/${req.pathVariable("reference")}"
        return datacoreService.deleteResource("marchepublic_0", "marchepublic:consultation_0", iri, bearer)
                .flatMap { result ->
                    ok().contentType(MediaType.APPLICATION_JSON)
                            .body(BodyInserters.empty<String>())
                }.onErrorResume { error ->
                    badRequest().body(BodyInserters.fromObject(error.message.orEmpty()))
                }
    }

    private fun extractBearer(headers: ServerRequest.Headers): String {
        val authorizationHeader = headers.header("Authorization")
        if (authorizationHeader.isEmpty() || authorizationHeader.size > 1)
            return ""

        return authorizationHeader[0].split(" ")[1]
    }
}