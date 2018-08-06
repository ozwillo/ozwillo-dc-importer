package org.ozwillo.dcimporter.repository

import org.ozwillo.dcimporter.model.BusinessAppConfiguration
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface BusinessAppConfigurationRepository : ReactiveMongoRepository<BusinessAppConfiguration, String> {
    fun findByDomainAndApplicationName(domain: String, applicationName: String): Mono<BusinessAppConfiguration>
    fun findByApplicationName(applicationName: String): Mono<BusinessAppConfiguration>
}