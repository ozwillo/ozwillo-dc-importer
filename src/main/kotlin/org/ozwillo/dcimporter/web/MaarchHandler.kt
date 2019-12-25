package org.ozwillo.dcimporter.web

import com.fasterxml.jackson.annotation.JsonProperty
import org.ozwillo.dcimporter.model.datacore.DCResource
import org.ozwillo.dcimporter.service.DatacoreService
import org.ozwillo.dcimporter.service.KernelService
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.ok
import org.springframework.web.reactive.function.server.bodyToMono
import reactor.core.publisher.Mono

@Component
class MaarchHandler(
    private val datacoreService: DatacoreService,
    private val kernelService: KernelService
) {

    @Value("\${datacore.model.project}")
    private val datacoreProject: String = "datacoreProject"

    @Value("\${datacore.model.modelEM}")
    private val datacoreModelEM: String = "datacoreModelEM"

    data class MaarchStatusRequest(@JsonProperty("publikId") val dcId: String)
    data class MaarchResponse(val returnCode: Int)

    fun status(req: ServerRequest): Mono<ServerResponse> =
        req.bodyToMono<MaarchStatusRequest>()
            .zipWhen {
                kernelService.getAccessToken()
            }
            .flatMap { maarchStatusWithToken ->
                val iri = maarchStatusWithToken.t1.dcId.substringAfter(datacoreModelEM)
                datacoreService.getResourceFromIRI(datacoreProject, datacoreModelEM, iri, maarchStatusWithToken.t2)
                    .map { Pair(it, maarchStatusWithToken.t2) }
            }.flatMap {
                // TODO : not sure we really neeed to do that
                val values = it.first.getValues().filter { entry ->
                    entry.key.startsWith("citizenreq")
                }
                val updatedResource = DCResource(it.first.getUri(), values)
                updatedResource.setStringValue("citizenreq:workflowStatus", "Terminé")
                datacoreService.updateResource(datacoreProject, datacoreModelEM, updatedResource, it.second)
            }.flatMap {
                ok().bodyValue(MaarchResponse(200))
            }
}
