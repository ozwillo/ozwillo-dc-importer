package org.ozwillo.dcimporter.repository

import org.ozwillo.dcimporter.model.BusinessMapping
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface BusinessMappingRepository : ReactiveMongoRepository<BusinessMapping, String> {

    fun findByDcIdAndApplicationName(dcId: String, applicationName: String): Mono<BusinessMapping>

    fun findByDcIdAndApplicationNameAndType(dcId: String, applicationName: String, type: String): Mono<BusinessMapping>

    fun findByBusinessIdAndApplicationNameAndType(
        businessId: String,
        applicationName: String,
        type: String
    ): Mono<BusinessMapping>

    fun deleteByDcIdAndApplicationNameAndType(
        dcId: String,
        applicationName: String,
        type: String
    ): Mono<BusinessMapping>
}