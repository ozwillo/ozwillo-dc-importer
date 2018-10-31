package org.ozwillo.dcimporter.repository

import org.ozwillo.dcimporter.model.BusinessAppConfiguration
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface BusinessAppConfigurationRepository : ReactiveMongoRepository<BusinessAppConfiguration, String> {
    fun findByApplicationName(applicationName: String): Flux<BusinessAppConfiguration>

    fun findByOrganizationSiretAndApplicationName(
        siret: String,
        applicationName: String
    ): Mono<BusinessAppConfiguration>

    fun deleteByOrganizationSiretAndApplicationName(
        siret: String,
        applicationName: String
    ): Mono<BusinessAppConfiguration>
}