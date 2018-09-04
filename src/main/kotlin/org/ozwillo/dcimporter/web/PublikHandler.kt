package org.ozwillo.dcimporter.web

import com.fasterxml.jackson.annotation.JsonProperty
import org.ozwillo.dcimporter.model.publik.FormModel
import org.ozwillo.dcimporter.service.DatacoreService
import org.ozwillo.dcimporter.service.PublikService
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters.fromObject
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.bodyToMono
import reactor.core.publisher.Mono

@Component
class PublikHandler(private val datacoreService: DatacoreService,
                    private val publikService: PublikService) {

    @Value("\${datacore.model.project}")
    private val datacoreProject: String = "datacoreProject"

    data class PublikDataResponse(@JsonProperty("data") val data: PublikDataEntryResponse)

    data class PublikDataEntryResponse(
            @JsonProperty("transmission_status") val transmissionStatus: Int,
            @JsonProperty("transmission_response") val transmissionResponse: Map<String, String>)

    fun publish(req: ServerRequest): Mono<ServerResponse> =
            req.bodyToMono<FormModel>()
                    .map { formModel -> publikService.formToDCResource(req.pathVariable("siret"), formModel) }
                    .flatMap { dcResourceWithProject ->
                        datacoreService.saveResource(datacoreProject, dcResourceWithProject.first, dcResourceWithProject.second, null)
                    }
                    .flatMap { result ->
                        ServerResponse.ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(fromObject(PublikDataResponse(PublikDataEntryResponse(HttpStatus.OK.value(), mapOf(Pair("Identifiant Ozwillo", result.resource.getUri()))))))
                    }
}
