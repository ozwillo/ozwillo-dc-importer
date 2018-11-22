package org.ozwillo.dcimporter.web

import org.ozwillo.dcimporter.model.AccessRequestState
import org.ozwillo.dcimporter.model.DataAccessRequest
import org.ozwillo.dcimporter.model.datacore.DCOperator
import org.ozwillo.dcimporter.model.datacore.DCOrdering
import org.ozwillo.dcimporter.model.datacore.DCQueryParameters
import org.ozwillo.dcimporter.model.sirene.Organization
import org.ozwillo.dcimporter.repository.DataAccessRequestRepository
import org.ozwillo.dcimporter.service.DatacoreService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.*
import org.springframework.web.reactive.function.server.body
import org.springframework.web.reactive.function.server.bodyToMono
import reactor.core.publisher.Mono

@Component
class DataAccessRequestHandler(
    private val dataAccessRequestRepository: DataAccessRequestRepository,
    private val datacoreService: DatacoreService
) {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(DataAccessRequestHandler::class.java)
    }

    @Value("\${datacore.model.modelORG}")
    private val modelOrg = ""
  
    fun getModels(req: ServerRequest): Mono<ServerResponse>{

        val name = if (req.queryParam("name").isPresent) req.queryParam("name").get() else ""

        return try {
            val dcModels = datacoreService.findModels(100, name)
                .map { it ->
                    val modelName = it.getStringValue("dcmo:name")
                    modelName
                }
                .collectList()
            ok().contentType(MediaType.APPLICATION_JSON).body(dcModels)
        }catch (e: HttpClientErrorException){
            status(e.statusCode).body(BodyInserters.fromObject(e.message!!))
        }
    }

    fun get(req: ServerRequest): Mono<ServerResponse>{

        val state = req.pathVariable("state").toUpperCase()

        return try {
            val dataAccessRequest = dataAccessRequestRepository.findByState(state)
            ok().contentType(MediaType.APPLICATION_JSON).body(dataAccessRequest, DataAccessRequest::class.java)
        }catch (e: Exception){
            this.throwableToResponse(e)
        }

    }

    fun getAllOrganization(req: ServerRequest): Mono<ServerResponse>{

        val queryParameter:String
        val queryObject:String

        when {
            req.queryParam("name").isPresent -> {
                queryParameter = req.queryParam("name").get()
                queryObject = "org:legalName"
            }
            req.queryParam("siret").isPresent -> {
                queryParameter = req.queryParam("siret").get()
                queryObject = "org:regNumber"
            }
            else -> return status(HttpStatus.BAD_REQUEST).body(BodyInserters.fromObject("Missing query parameter \"name\" or \"siret\""))
        }

        return try {
            val organizations = datacoreService.findResources(
                "oasis.main",
                modelOrg,
                DCQueryParameters(queryObject, DCOperator.REGEX, DCOrdering.DESCENDING, queryParameter),
                0,
                100
            )
                .map { dcOrganization ->
                    val organization = Organization.fromDcObject(dcOrganization)
                    organization
                }
            ok().contentType(MediaType.APPLICATION_JSON).body(organizations, Organization::class.java)

        }catch (e: HttpClientErrorException){
            status(e.statusCode).body(BodyInserters.fromObject(e.message!!))
        }
    }

    fun create(req: ServerRequest): Mono<ServerResponse> {
        val request = req.pathVariable("request")

        return req.bodyToMono<DataAccessRequest>()
            .flatMap { dataAccessRequest ->

                if (request == "send")
                    dataAccessRequest.state = AccessRequestState.SENT
                else
                    dataAccessRequest.state = AccessRequestState.SAVED

                dataAccessRequestRepository.save(dataAccessRequest).subscribe()
                status(HttpStatus.CREATED).body(BodyInserters.empty<String>())
            }
            .onErrorResume(this::throwableToResponse)
    }

    fun update(req: ServerRequest): Mono<ServerResponse>{
        val id = req.pathVariable("id")
        val state =
            when(req.pathVariable("action")) {
                "valid" -> AccessRequestState.VALIDATED
                "reject" -> AccessRequestState.REFUSED
                "save" -> AccessRequestState.SAVED
                else -> return status(HttpStatus.BAD_REQUEST).body(BodyInserters.fromObject("Unable to recognize requested action. Waiting for \"valid\" or \"reject\" or \"save\""))
            }

        val fallback: Mono<DataAccessRequest> = Mono.error(EmptyException("No data access request found with id $id"))

        return dataAccessRequestRepository.findById(id)
            .switchIfEmpty(fallback)
            .flatMap { currentDataAccessRequest ->
                req.bodyToMono<DataAccessRequest>()
                    .flatMap { dataAccessRequest ->
                        dataAccessRequestRepository.save(
                            DataAccessRequest(
                                id = currentDataAccessRequest.id,
                                nom = if (state == AccessRequestState.SAVED) dataAccessRequest.nom else currentDataAccessRequest.nom,
                                model = if (state == AccessRequestState.SAVED) dataAccessRequest.model else currentDataAccessRequest.model,
                                organization = if (state == AccessRequestState.SAVED) dataAccessRequest.organization else currentDataAccessRequest.organization,
                                email = if (state == AccessRequestState.SAVED) dataAccessRequest.email else currentDataAccessRequest.email,
                                creationDate = if (state == AccessRequestState.SAVED) dataAccessRequest.creationDate else currentDataAccessRequest.creationDate,
                                state = state
                            )
                        ).subscribe()
                        ok().body(BodyInserters.empty<String>())
                    }
            }
            .onErrorResume { e ->
                when (e) {
                    is EmptyException -> status(HttpStatus.NOT_FOUND).body(BodyInserters.fromObject(e.message))
                    else -> this.throwableToResponse(e)
                }
            }
    }

    fun dataAccess(req: ServerRequest): Mono<ServerResponse>{
        val id = req.pathVariable("id")
        return try {
            val dataAccessRequest = dataAccessRequestRepository.findById(id)
            ok().contentType(MediaType.APPLICATION_JSON).body(dataAccessRequest, DataAccessRequest::class.java)
        }catch (e: Exception){
            this.throwableToResponse(e)
        }
    }

    //Utils

    private fun throwableToResponse(throwable: Throwable): Mono<ServerResponse> {
        DataAccessRequestHandler.LOGGER.error("Operation failed with error $throwable")
        return when (throwable) {
            is HttpClientErrorException -> ServerResponse.badRequest().body(BodyInserters.fromObject(throwable.responseBodyAsString))
            else -> {
                ServerResponse.badRequest().body(BodyInserters.fromObject(throwable.message.orEmpty()))
            }
        }

    }

}