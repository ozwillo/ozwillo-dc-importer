package org.ozwillo.dcimporter.repository

import com.mongodb.client.result.DeleteResult
import org.ozwillo.dcimporter.model.BusinessAppConfiguration
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
interface BusinessAppConfigurationRepository : ReactiveMongoRepository<BusinessAppConfiguration, String> {
    fun findByApplicationName(applicationName: String): Flux<BusinessAppConfiguration>

    fun findAllByApplicationName(applicationName: String): Flux<BusinessAppConfiguration>

    fun findByOrganizationSiretAndApplicationName(siret: String, applicationName: String): Mono<BusinessAppConfiguration>

    fun findByOrganizationSiretAndApplicationNameAndLogin(siret: String, applicationName: String, login: String): Mono<BusinessAppConfiguration>

    fun deleteByOrganizationSiretAndApplicationNameAndLogin(siret: String, applicationName: String, login: String):Mono<DeleteResult>
}