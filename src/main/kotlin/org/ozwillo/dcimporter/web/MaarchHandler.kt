package org.ozwillo.dcimporter.web

import com.fasterxml.jackson.annotation.JsonProperty
import org.ozwillo.dcimporter.model.datacore.DCResource
import org.ozwillo.dcimporter.service.DatacoreService
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.bodyToMono
import reactor.core.publisher.Mono

@Component
class MaarchHandler(private val datacoreService: DatacoreService) {

    @Value("\${datacore.model.project}")
    private val datacoreProject: String = "datacoreProject"

    @Value("\${datacore.model.modelEM}")
    private val datacoreModelEM: String = "datacoreModelEM"

    data class MaarchStatusRequest(@JsonProperty("publikId") val dcId: String)
    data class MaarchResponse(val returnCode: Int)

    fun status(req: ServerRequest): Mono<ServerResponse> =
        req.bodyToMono<MaarchStatusRequest>()
            .flatMap { maarchRequest ->
                val iri = maarchRequest.dcId.substringAfter(datacoreModelEM)
                datacoreService.getResourceFromIRI(datacoreProject, datacoreModelEM, iri, null)
            }.flatMap {
                // TODO : not sure we really neeed to do that
                val values = it.getValues().filter { entry ->
                    entry.key.startsWith("citizenreq")
                }
                val updatedResource = DCResource(it.getUri(), values)
                updatedResource.setStringValue("citizenreq:workflowStatus", "Terminé")
                datacoreService.updateResource(datacoreProject, datacoreModelEM, updatedResource, null)
            }.flatMap {
                ServerResponse.status(it).body(BodyInserters.fromObject(MaarchResponse(it.value())))
            }
}
