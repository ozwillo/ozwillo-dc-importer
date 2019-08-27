package org.ozwillo.dcimporter.repository

import org.ozwillo.dcimporter.model.DataAccessRequest
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux

@Repository
interface DataAccessRequestRepository : ReactiveMongoRepository<DataAccessRequest, String> {

    fun findByState(state: String): Flux<DataAccessRequest>
}
