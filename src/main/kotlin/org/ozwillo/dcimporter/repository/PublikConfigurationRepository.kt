package org.ozwillo.dcimporter.repository

import org.ozwillo.dcimporter.model.publik.PublikConfiguration
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface PublikConfigurationRepository : ReactiveMongoRepository<PublikConfiguration, String> {
    fun findByDomain(domain: String): Mono<PublikConfiguration>
}