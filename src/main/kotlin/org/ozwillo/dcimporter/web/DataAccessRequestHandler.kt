package org.ozwillo.dcimporter.web

import org.ozwillo.dcimporter.model.AccessRequestState
import org.ozwillo.dcimporter.model.DataAccessRequest
import org.ozwillo.dcimporter.repository.DataAccessRequestRepository
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.status
import org.springframework.web.reactive.function.server.bodyToMono
import reactor.core.publisher.Mono

@Component
class DataAccessRequestHandler(
    private val dataAccessRequestRepository: DataAccessRequestRepository
) {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(DataAccessRequestHandler::class.java)
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