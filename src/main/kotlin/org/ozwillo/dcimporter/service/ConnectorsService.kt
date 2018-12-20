package org.ozwillo.dcimporter.service

import org.ozwillo.dcimporter.model.BusinessAppConfiguration
import org.ozwillo.dcimporter.repository.BusinessAppConfigurationRepository
import org.ozwillo.dcimporter.web.ConflictException
import org.ozwillo.dcimporter.web.EmptyException
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

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

    fun create(siret: String, appName: String, businessAppConfiguration: BusinessAppConfiguration): Mono<BusinessAppConfiguration> {

        val fallback: Mono<BusinessAppConfiguration> =
            Mono.error(ConflictException("Connectors have already been created for application \"$appName\" and siret \"$siret\", please update"))

        return businessAppConfigurationRepository.findByOrganizationSiretAndApplicationName(siret, appName)
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
                    )
                }else{
                    fallback
                }
            }
    }

    fun clone(businessAppConfiguration: BusinessAppConfiguration): Mono<BusinessAppConfiguration>{
        val fallback: Mono<BusinessAppConfiguration> =
                Mono.error(EmptyException("No connector found with id ${businessAppConfiguration.id!!}"))

        return businessAppConfigurationRepository.findById(businessAppConfiguration.id)
            .switchIfEmpty(fallback)
            .flatMap { existingConnector ->
                val connector =
                    BusinessAppConfiguration(
                        baseUrl = existingConnector.baseUrl,
                        organizationSiret = businessAppConfiguration.organizationSiret,
                        instanceId = businessAppConfiguration.instanceId,
                        login = businessAppConfiguration.login,
                        password = businessAppConfiguration.password,
                        applicationName = existingConnector.applicationName,
                        secretOrToken = businessAppConfiguration.secretOrToken
                    )
                create(businessAppConfiguration.organizationSiret, existingConnector.applicationName, connector)
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

    fun delete(id: String): Mono<BusinessAppConfiguration>{
        val fallback: Mono<BusinessAppConfiguration> =
                Mono.error(EmptyException("Connector not found for id: \"$id\""))

        return businessAppConfigurationRepository.findById(id)
            .switchIfEmpty(fallback)
            .flatMap {
                businessAppConfigurationRepository.deleteById(id)
                    .subscribe()
                Mono.empty<BusinessAppConfiguration>()
            }
    }

    fun deleteBySiretAndAppName(siret: String, appName: String):Mono<BusinessAppConfiguration>{
        val fallback: Mono<BusinessAppConfiguration> =
            Mono.error(EmptyException("Connector not found for application \"$appName\" and siret $siret"))

        return businessAppConfigurationRepository.findByOrganizationSiretAndApplicationName(siret, appName)
            .switchIfEmpty(fallback)
            .flatMap {
                businessAppConfigurationRepository.deleteByOrganizationSiretAndApplicationName(siret, appName)
                    .subscribe()
                Mono.empty<BusinessAppConfiguration>()
            }
    }

}