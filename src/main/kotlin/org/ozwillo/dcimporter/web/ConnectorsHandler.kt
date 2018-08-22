package org.ozwillo.dcimporter.web

import org.ozwillo.dcimporter.model.BusinessAppConfiguration
import org.ozwillo.dcimporter.repository.BusinessAppConfigurationRepository
import org.ozwillo.dcimporter.service.MaarchService
import org.ozwillo.dcimporter.service.MarcheSecuriseService
import org.ozwillo.dcimporter.service.PublikService
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
class ConnectorsHandler(private val businessAppConfigurationRepository: BusinessAppConfigurationRepository){

    companion object {
       private val LOGGER = LoggerFactory.getLogger(ConnectorsHandler::class.java)
    }

    fun getAllByAppName(req: ServerRequest): Mono<ServerResponse>{
        val appName = applicationNameFormatter(req.pathVariable("applicationName"))

        return try {
            val businessAppConfiguration:Flux<BusinessAppConfiguration> = businessAppConfigurationRepository.findAllByApplicationName(appName)
            ok().contentType(MediaType.APPLICATION_JSON).body(businessAppConfiguration, BusinessAppConfiguration::class.java)
        }catch (e: HttpClientErrorException){
            when(e.statusCode){
                HttpStatus.NOT_FOUND -> status(e.statusCode).body(BodyInserters.fromObject("Nothing found for application name : $appName"))
                else -> status(e.statusCode).body(BodyInserters.fromObject("Unexpected error"))
            }
        }
    }

    fun createNewConnectors(req: ServerRequest): Mono<ServerResponse> {
        val siret = req.pathVariable("siret")

        return req.bodyToMono<BusinessAppConfiguration>()
                .flatMap { businessAppConfiguration ->
                    businessAppConfigurationRepository.findByOrganizationSiretAndApplicationName(siret, businessAppConfiguration.applicationName)
                            .defaultIfEmpty(BusinessAppConfiguration(applicationName = businessAppConfiguration.applicationName, organizationSiret = siret, baseUrl = businessAppConfiguration.baseUrl))
                            .flatMap { existingConnector ->
                                if (existingConnector.login == null) {
                                    businessAppConfigurationRepository.save(BusinessAppConfiguration(baseUrl = businessAppConfiguration.baseUrl,
                                            organizationSiret = siret,
                                            instanceId = businessAppConfiguration.instanceId, login = businessAppConfiguration.login, password = businessAppConfiguration.password,
                                            applicationName = businessAppConfiguration.applicationName)).subscribe()
                                    status(HttpStatus.CREATED).body(BodyInserters.empty<String>())
                                } else {
                                    status(HttpStatus.CONFLICT).body(BodyInserters.fromObject("Connectors have already been created for application \"${businessAppConfiguration.applicationName}\" and siret $siret, please update"))
                                }
                            }
                }
                .onErrorResume(this::throwableToResponse)
    }

    fun updateConnectors(req: ServerRequest): Mono<ServerResponse>{
        val siret = req.pathVariable("siret")
        val appName = applicationNameFormatter(req.pathVariable("applicationName"))
        val login = req.pathVariable("login")

        val fallback: Mono<BusinessAppConfiguration> = Mono.error(EmptyException("Connector with login \"$login\" and application \"$appName\" was not found for siret $siret"))

        return businessAppConfigurationRepository.findByOrganizationSiretAndApplicationNameAndLogin(siret, appName, login)
                .switchIfEmpty(fallback)
                .flatMap { existingConnector ->
                    req.bodyToMono<BusinessAppConfiguration>()
                            .flatMap {businessAppConfiguration ->

                                businessAppConfigurationRepository.save(BusinessAppConfiguration(id = existingConnector.id, baseUrl = businessAppConfiguration.baseUrl, organizationSiret = siret,
                                        instanceId = businessAppConfiguration.instanceId, login = businessAppConfiguration.login, password = businessAppConfiguration.password, applicationName = businessAppConfiguration.applicationName)).subscribe()
                                ok().body(BodyInserters.empty<String>())
                            }
                }
                .onErrorResume{e ->
                    when(e){
                        is EmptyException -> status(HttpStatus.NOT_FOUND).body(BodyInserters.fromObject(e.message))
                        else -> this.throwableToResponse(e)
                    }
                }
    }

    fun deleteConnectors(req: ServerRequest): Mono<ServerResponse>{
        val siret = req.pathVariable("siret")
        val appName = applicationNameFormatter(req.pathVariable("applicationName"))
        val login = req.pathVariable("login")

        val fallback: Mono<BusinessAppConfiguration> = Mono.error(EmptyException("Connector with login \"$login\" and application \"$appName\" was not found for siret $siret"))

        return businessAppConfigurationRepository.findByOrganizationSiretAndApplicationNameAndLogin(siret, appName, login)
                .switchIfEmpty(fallback)
                .flatMap { _ ->
                    businessAppConfigurationRepository.deleteByOrganizationSiretAndApplicationNameAndLogin(siret, appName, login).subscribe()
                    status(HttpStatus.NO_CONTENT).body(BodyInserters.empty<String>())
                }
                .onErrorResume{e ->
                    when(e){
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

    private fun applicationNameFormatter(pathAppName: String):String {
        return when (pathAppName) {
            "marche-securise" -> MarcheSecuriseService.name
            "publik" -> PublikService.name
            "maarch" -> MaarchService.name
            else -> {
                LOGGER.error("Unable to recognize application from $pathAppName")
                "error"
            }
        }
    }
}

class EmptyException(override var message: String): Exception(message) {
}