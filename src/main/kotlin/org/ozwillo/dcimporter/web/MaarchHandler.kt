package org.ozwillo.dcimporter.web

import com.fasterxml.jackson.annotation.JsonProperty
import org.ozwillo.dcimporter.repository.BusinessMappingRepository
import org.ozwillo.dcimporter.service.MaarchService
import org.ozwillo.dcimporter.service.PublikService
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.bodyToMono

@Component
class MaarchHandler(private val businessMappingRepository: BusinessMappingRepository,
                    private val publikService: PublikService) {

    data class MaarchStatusRequest(@JsonProperty("publikId") val dcId: String)
    data class MaarchResponse(val returnCode: Int)

    fun status(req: ServerRequest) =
            req.bodyToMono<MaarchStatusRequest>()
                    .flatMap { maarchRequest ->
                        businessMappingRepository.findByDcIdAndApplicationName(maarchRequest.dcId, PublikService.name)
                    }.map { publikMapping ->
                        publikService.changeStatus(publikMapping.businessId)
                    }.flatMap {
                        ServerResponse.ok().body(BodyInserters.fromObject(MaarchResponse(it.err!!)))
                    }
}