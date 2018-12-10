package org.ozwillo.dcimporter.service

import org.ozwillo.dcimporter.model.BusinessAppConfiguration
import org.ozwillo.dcimporter.repository.BusinessAppConfigurationRepository
import org.ozwillo.dcimporter.web.EmptyException
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.bodyToMono
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.toMono

@Service
class ConnectorsService(private val businessAppConfigurationRepository: BusinessAppConfigurationRepository){

    fun searchConnectors(siret: String, appName: String): Flux<BusinessAppConfiguration> {
        return when {
            !siret.isEmpty() && !appName.isEmpty() -> this.getAllBySiretAndContainingAppName(siret, appName)
            !siret.isEmpty() && appName.isEmpty() -> this.getAllBySiret(siret)
            siret.isEmpty() && !appName.isEmpty() -> this.getAllContainingAppName(appName)
            else -> this.getAll()
        }
    }

    fun getAll(): Flux<BusinessAppConfiguration> {
        return businessAppConfigurationRepository.findAll()
    }

    fun getAllByAppName(appName: String): Flux<BusinessAppConfiguration> {
        return businessAppConfigurationRepository.findByApplicationName(appName)
    }

    fun getById(id: String): Mono<BusinessAppConfiguration> {
        return businessAppConfigurationRepository.findById(id)
    }

    fun getAllBySiret(siret: String): Flux<BusinessAppConfiguration> {
        return businessAppConfigurationRepository.findByOrganizationSiret(siret)
    }

    fun getAllContainingAppName(appName: String): Flux<BusinessAppConfiguration>{
        return businessAppConfigurationRepository.findByApplicationNameIgnoreCaseContaining(appName)
    }

    fun getAllBySiretAndContainingAppName(siret: String, appName: String): Flux<BusinessAppConfiguration> {
        return businessAppConfigurationRepository.findByOrganizationSiretAndApplicationNameIgnoreCaseContaining(siret, appName)
    }

    fun create(siret: String, appName: String, businessAppConfiguration: BusinessAppConfiguration): Mono<HttpStatus> {

        return businessAppConfigurationRepository.findByOrganizationSiretAndApplicationName(siret, appName)
            .defaultIfEmpty(
                BusinessAppConfiguration(
                    applicationName = "",
                    organizationSiret = siret,
                    baseUrl = businessAppConfiguration.baseUrl
                )
            )
            .map { existingConnector ->
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
                    HttpStatus.CREATED
                }else{
                    HttpStatus.CONFLICT
                }
            }
            .onErrorResume { e ->
                HttpStatus.BAD_REQUEST.toMono()
            }
    }

    fun update(siret: String, appName: String, monoBusinessAppConfiguration: Mono<BusinessAppConfiguration>):Mono<BusinessAppConfiguration>{

        val fallback: Mono<BusinessAppConfiguration> =
            Mono.error(EmptyException("Connector not found for application \"$appName\" and siret $siret"))

        return businessAppConfigurationRepository.findByOrganizationSiretAndApplicationName(siret, appName)
            .switchIfEmpty(fallback)
            .flatMap { existingConnector ->
                monoBusinessAppConfiguration
                    .map { businessAppConfiguration ->

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
                        businessAppConfiguration
                    }
            }
    }

    fun delete(siret: String, appName: String):Mono<BusinessAppConfiguration>{
        val fallback: Mono<BusinessAppConfiguration> =
            Mono.error(EmptyException("Connector not found for application \"$appName\" and siret $siret"))

        return businessAppConfigurationRepository.findByOrganizationSiretAndApplicationName(siret, appName)
            .switchIfEmpty(fallback)
            .map {businessAppConfiguration ->
                businessAppConfigurationRepository.deleteByOrganizationSiretAndApplicationName(siret, appName)
                    .subscribe()
                businessAppConfiguration
            }
    }

}