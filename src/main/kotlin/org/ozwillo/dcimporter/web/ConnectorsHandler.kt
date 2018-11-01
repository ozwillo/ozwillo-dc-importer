package org.ozwillo.dcimporter.web

import org.ozwillo.dcimporter.model.BusinessAppConfiguration
import org.ozwillo.dcimporter.repository.BusinessAppConfigurationRepository
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
class ConnectorsHandler(private val businessAppConfigurationRepository: BusinessAppConfigurationRepository) {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(ConnectorsHandler::class.java)
    }

    fun getAllByAppName(req: ServerRequest): Mono<ServerResponse> {
        val appName = req.pathVariable("applicationName")

        return try {
            val businessAppConfiguration: Flux<BusinessAppConfiguration> =
                businessAppConfigurationRepository.findByApplicationName(appName)
            ok().contentType(MediaType.APPLICATION_JSON)
                .body(businessAppConfiguration, BusinessAppConfiguration::class.java)
        } catch (e: Exception) {
            this.throwableToResponse(e)
        }
    }

    fun createNewConnectors(req: ServerRequest): Mono<ServerResponse> {
        val siret = req.pathVariable("siret")
        val appName = req.pathVariable("applicationName")

        return req.bodyToMono<BusinessAppConfiguration>()
            .flatMap { businessAppConfiguration ->
                businessAppConfigurationRepository.findByOrganizationSiretAndApplicationName(siret, appName)
                    .defaultIfEmpty(
                        BusinessAppConfiguration(
                            applicationName = "",
                            organizationSiret = siret,
                            baseUrl = businessAppConfiguration.baseUrl
                        )
                    )
                    .flatMap { existingConnector ->
                        if (existingConnector.applicationName.isEmpty()) {
                            businessAppConfigurationRepository.save(
                                BusinessAppConfiguration(
                                    baseUrl = businessAppConfiguration.baseUrl,
                                    organizationSiret = siret,
                                    instanceId = businessAppConfiguration.instanceId,
                                    login = businessAppConfiguration.login,
                                    password = businessAppConfiguration.password,
                                    applicationName = appName,
                                    secretOrToken = businessAppConfiguration.secretOrToken
                                )
                            ).subscribe()
                            status(HttpStatus.CREATED).body(BodyInserters.empty<String>())
                        } else {
                            status(HttpStatus.CONFLICT).body(
                                BodyInserters.fromObject(
                                    "Connectors have already been created for application \"$appName\" and siret $siret, please update"))
                        }
                    }
            }
            .onErrorResume(this::throwableToResponse)
    }

    fun updateConnectors(req: ServerRequest): Mono<ServerResponse> {
        val siret = req.pathVariable("siret")
        val appName = req.pathVariable("applicationName")

        val fallback: Mono<BusinessAppConfiguration> =
            Mono.error(EmptyException("Connector not found for application \"$appName\" and siret $siret"))

        return businessAppConfigurationRepository.findByOrganizationSiretAndApplicationName(siret, appName)
            .switchIfEmpty(fallback)
            .flatMap { existingConnector ->
                req.bodyToMono<BusinessAppConfiguration>()
                    .flatMap { businessAppConfiguration ->

                        businessAppConfigurationRepository.save(
                            BusinessAppConfiguration(
                                id = existingConnector.id,
                                baseUrl = businessAppConfiguration.baseUrl,
                                organizationSiret = siret,
                                instanceId = businessAppConfiguration.instanceId,
                                login = businessAppConfiguration.login,
                                password = businessAppConfiguration.password,
                                applicationName = appName,
                                secretOrToken = businessAppConfiguration.secretOrToken
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

    fun deleteConnectors(req: ServerRequest): Mono<ServerResponse> {
        val siret = req.pathVariable("siret")
        val appName = req.pathVariable("applicationName")

        val fallback: Mono<BusinessAppConfiguration> =
            Mono.error(EmptyException("Connector not found for application \"$appName\" and siret $siret"))

        return businessAppConfigurationRepository.findByOrganizationSiretAndApplicationName(siret, appName)
            .switchIfEmpty(fallback)
            .flatMap { _ ->
                businessAppConfigurationRepository.deleteByOrganizationSiretAndApplicationName(siret, appName)
                    .subscribe()
                status(HttpStatus.NO_CONTENT).body(BodyInserters.empty<String>())
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