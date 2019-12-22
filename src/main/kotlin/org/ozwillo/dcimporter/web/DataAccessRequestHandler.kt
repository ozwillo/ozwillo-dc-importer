package org.ozwillo.dcimporter.web

import javax.mail.SendFailedException
import org.ozwillo.dcimporter.model.AccessRequestState
import org.ozwillo.dcimporter.model.DataAccessRequest
import org.ozwillo.dcimporter.repository.DataAccessRequestRepository
import org.ozwillo.dcimporter.service.EmailService
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.*
import org.springframework.web.reactive.function.server.bodyToMono
import reactor.core.publisher.Mono

@Component
class DataAccessRequestHandler(
    private val dataAccessRequestRepository: DataAccessRequestRepository,
    private val emailService: EmailService
) {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(DataAccessRequestHandler::class.java)
    }

    fun get(req: ServerRequest): Mono<ServerResponse> {

        val state = if (req.queryParam("state").isPresent) req.queryParam("state").get().toUpperCase() else ""

        return try {
            val dataAccessRequest = dataAccessRequestRepository.findByState(state)
            ok().contentType(MediaType.APPLICATION_JSON).body(dataAccessRequest, DataAccessRequest::class.java)
        } catch (e: Exception) {
            this.throwableToResponse(e)
        }
    }

    fun create(req: ServerRequest): Mono<ServerResponse> {

        return req.bodyToMono<DataAccessRequest>()
            .flatMap { dataAccessRequest ->

                dataAccessRequest.state = AccessRequestState.SENT
                dataAccessRequestRepository.save(dataAccessRequest).subscribe()

                status(HttpStatus.CREATED).body(BodyInserters.empty<String>())
            }
            .onErrorResume(this::throwableToResponse)
    }

    fun update(req: ServerRequest): Mono<ServerResponse> {
        val id = req.pathVariable("id")
        val state =
            when (req.pathVariable("action")) {
                "valid" -> AccessRequestState.VALIDATED
                "reject" -> AccessRequestState.REFUSED
                // "save" -> AccessRequestState.SAVED
                else -> return status(HttpStatus.BAD_REQUEST).body(BodyInserters.fromValue("Unable to recognize requested action. Waiting for \"valid\" or \"reject\""))
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
                                nom = currentDataAccessRequest.nom,
                                model = currentDataAccessRequest.model,
                                organization = currentDataAccessRequest.organization,
                                email = currentDataAccessRequest.email,
                                creationDate = currentDataAccessRequest.creationDate,
                                state = state,
                                fields = currentDataAccessRequest.fields
                            )
                        ).subscribe()

                        try { // TODO: #5350 decoupling of the e-mail sending feature
                            emailService.sendSimpleMessage(
                                currentDataAccessRequest.email,
                                "[DC-Importer] Your data access request ${state.name.toLowerCase()}",
                                "The data access request for ${currentDataAccessRequest.model} model was ${state.name.toLowerCase()}")
                        } catch (sendFailedException: SendFailedException) {
                            throwableToResponse(sendFailedException)
                        }

                        ok().contentType(MediaType.APPLICATION_JSON).body(BodyInserters.empty<String>())
                    }
            }
            .onErrorResume { e ->
                when (e) {
                    is EmptyException -> status(HttpStatus.NOT_FOUND).body(BodyInserters.fromValue(e.message))
                    else -> this.throwableToResponse(e)
                }
            }
    }

    fun dataAccess(req: ServerRequest): Mono<ServerResponse> {
        val id = req.pathVariable("id")
        return try {
            val dataAccessRequest = dataAccessRequestRepository.findById(id)
            ok().contentType(MediaType.APPLICATION_JSON).body(dataAccessRequest, DataAccessRequest::class.java)
        } catch (e: Exception) {
            this.throwableToResponse(e)
        }
    }

    // Utils

    private fun throwableToResponse(throwable: Throwable): Mono<ServerResponse> {
        LOGGER.error("Operation failed with error $throwable")
        return when (throwable) {
            is HttpClientErrorException -> badRequest().body(BodyInserters.fromValue(throwable.responseBodyAsString))
            else -> badRequest().body(BodyInserters.fromValue(throwable.message.orEmpty()))
        }
    }
}
