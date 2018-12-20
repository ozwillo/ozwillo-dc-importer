package org.ozwillo.dcimporter.web

import org.ozwillo.dcimporter.model.BusinessAppConfiguration
import org.ozwillo.dcimporter.service.ConnectorsService
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
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Component
class ConnectorsHandler(private val connectorsService: ConnectorsService) {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(ConnectorsHandler::class.java)
    }

    fun getAllWithoutSecret(req: ServerRequest): Mono<ServerResponse> {

        val siret = if (req.queryParam("siret").isPresent) req.queryParam("siret").get() else ""
        val appName = if (req.queryParam("application").isPresent) req.queryParam("application").get() else ""

        return try {
                val businessAppConfiguration: Flux<BusinessAppConfiguration> =
                    connectorsService.searchConnectors(siret, appName)
                        .map { connector ->
                            BusinessAppConfiguration(
                                id = connector.id,
                                applicationName = connector.applicationName,
                                displayName = connector.displayName,
                                organizationSiret = connector.organizationSiret,
                                baseUrl = connector.baseUrl)
                        }
                ok().contentType(MediaType.APPLICATION_JSON)
                    .body(businessAppConfiguration, BusinessAppConfiguration::class.java)
        }catch (e: HttpClientErrorException){
                this.throwableToResponse(e)
        }
    }

    fun get(req: ServerRequest): Mono<ServerResponse> {
        val id = req.pathVariable("id")

        return try {
            val businessAppConfiguration: Mono<BusinessAppConfiguration> =
                    connectorsService.getById(id)
            ok().contentType(MediaType.APPLICATION_JSON)
                .body(businessAppConfiguration, BusinessAppConfiguration::class.java)
        }catch (e: HttpClientErrorException){
            this.throwableToResponse(e)
        }
    }

    fun getAllByAppName(req: ServerRequest): Mono<ServerResponse> {
        val appName = req.pathVariable("applicationName")

        return try {
            val businessAppConfiguration: Flux<BusinessAppConfiguration> =
                connectorsService.getAllByAppName(appName)
            ok().contentType(MediaType.APPLICATION_JSON)
                .body(businessAppConfiguration, BusinessAppConfiguration::class.java)
        } catch (e: HttpClientErrorException) {
            this.throwableToResponse(e)
        }
    }

    fun createNewConnectors(req: ServerRequest): Mono<ServerResponse> {
        val siret = req.pathVariable("siret")
        val appName = req.pathVariable("applicationName")

        return req.bodyToMono<BusinessAppConfiguration>()
            .flatMap { businessAppConfiguration ->
                connectorsService.create(siret, appName, businessAppConfiguration)
            }
            .flatMap {
                status(HttpStatus.CREATED).contentType(MediaType.APPLICATION_JSON).body(BodyInserters.empty<String>())
            }
            .onErrorResume{ e ->
                when (e) {
                    is ConflictException -> status(HttpStatus.CONFLICT).body(BodyInserters.fromObject(e.message))
                    else -> this.throwableToResponse(e)
                }
            }
    }

    fun clone(req: ServerRequest): Mono<ServerResponse> {
        return req.bodyToMono<BusinessAppConfiguration>()
            .flatMap { connector ->
                connectorsService.clone(connector)
            }
            .flatMap {
                status(HttpStatus.CREATED).contentType(MediaType.APPLICATION_JSON).body(BodyInserters.empty<String>())
            }
            .onErrorResume { e ->
                when (e) {
                    is ConflictException -> status(HttpStatus.CONFLICT).body(BodyInserters.fromObject(e.message))
                    else -> this.throwableToResponse(e)
                }
            }
    }

    fun updateConnectors(req: ServerRequest): Mono<ServerResponse> {
        val siret = req.pathVariable("siret")
        val appName = req.pathVariable("applicationName")

        val monoBusinessAppConfiguration = req.bodyToMono<BusinessAppConfiguration>()

        return connectorsService.update(siret, appName, monoBusinessAppConfiguration)
            .flatMap {
                ok().contentType(MediaType.APPLICATION_JSON).body(BodyInserters.empty<String>())
            }
            .onErrorResume { e ->
                when (e) {
                    is EmptyException -> status(HttpStatus.NOT_FOUND).body(BodyInserters.fromObject(e.message))
                    else -> this.throwableToResponse(e)
                }
            }
    }

    fun delete(req: ServerRequest): Mono<ServerResponse> {
        val id = req.pathVariable("id")

        return connectorsService.delete(id)
            .flatMap {
                status(HttpStatus.NO_CONTENT).body(BodyInserters.empty<String>())
            }
            .onErrorResume { e ->
                when(e){
                    is EmptyException -> status(HttpStatus.NOT_FOUND).contentType(MediaType.APPLICATION_JSON).body(BodyInserters.fromObject(e.message))
                    else -> this.throwableToResponse(e)
                }
            }
    }

    fun deleteBySiretAndAppName(req: ServerRequest): Mono<ServerResponse> {
        val siret = req.pathVariable("siret")
        val appName = req.pathVariable("applicationName")

        return connectorsService.deleteBySiretAndAppName(siret, appName)
            .flatMap {
                ServerResponse.status(HttpStatus.NO_CONTENT).body(BodyInserters.empty<String>())
            }
            .onErrorResume { e ->
                when (e) {
                    is EmptyException -> status(HttpStatus.NOT_FOUND).body(BodyInserters.fromObject(e.message))
                    else -> this.throwableToResponse(e)
                }
            }
    }

    private fun throwableToResponse(throwable: Throwable): Mono<ServerResponse> {
        LOGGER.error("Operation failed with error $throwable")
        return when (throwable) {
            is HttpClientErrorException -> badRequest().body(BodyInserters.fromObject(throwable.responseBodyAsString))
            else -> {
                badRequest().body(BodyInserters.fromObject(throwable.message.orEmpty()))
            }
        }

    }
}

class EmptyException(override var message: String) : Exception(message)
class ConflictException(override var message: String) : Exception(message)