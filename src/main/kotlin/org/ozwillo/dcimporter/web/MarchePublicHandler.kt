package org.ozwillo.dcimporter.web

import org.ozwillo.dcimporter.config.DatacoreProperties
import org.ozwillo.dcimporter.model.marchepublic.Consultation
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.bodyToMono
import reactor.core.publisher.Mono
import java.net.URI

@Component
class MarchePublicHandler(@Autowired var datacoreProperties: DatacoreProperties) {

    fun create(req: ServerRequest): Mono<ServerResponse> =
        req.bodyToMono<Consultation>()
                .map { consultation -> consultation.toDcObject(datacoreProperties.baseUri) }
                .flatMap { dcConsultation ->
                    ServerResponse.created(URI(dcConsultation.getUri()))
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(BodyInserters.fromObject(dcConsultation))
                }
}