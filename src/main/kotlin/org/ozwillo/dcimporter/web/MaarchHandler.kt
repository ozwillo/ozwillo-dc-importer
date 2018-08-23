package org.ozwillo.dcimporter.web

import com.fasterxml.jackson.annotation.JsonProperty
import org.ozwillo.dcimporter.model.datacore.DCBusinessResourceLight
import org.ozwillo.dcimporter.service.DatacoreService
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.bodyToMono

@Component
class MaarchHandler(private val datacoreService: DatacoreService) {

    @Value("\${publik.datacore.project}")
    private val datacoreProject: String = "datacoreProject"

    @Value("\${publik.datacore.modelEM}")
    private val datacoreModelEM: String = "datacoreModelEM"

    data class MaarchStatusRequest(@JsonProperty("publikId") val dcId: String)
    data class MaarchResponse(val returnCode: Int)

    fun status(req: ServerRequest) =
            req.bodyToMono<MaarchStatusRequest>()
                    .flatMap { maarchRequest ->
                        val iri = maarchRequest.dcId.substringAfter(datacoreModelEM)
                        val dcResource = datacoreService.getResourceFromIRI(datacoreProject, datacoreModelEM, iri, null)
                        // TODO : not sure we really neeed to do that
                        val values = dcResource.getValues().filter { entry ->
                            entry.key.startsWith("citizenreq")
                        }
                        val updatedResource = DCBusinessResourceLight(maarchRequest.dcId, values)
                        updatedResource.setStringValue("citizenreq:workflowStatus", "Terminé")
                        datacoreService.updateResource(datacoreProject, datacoreModelEM, updatedResource, null)
                    }.flatMap {
                        ServerResponse.status(it).body(BodyInserters.fromObject(MaarchResponse(it.value())))
                    }
}