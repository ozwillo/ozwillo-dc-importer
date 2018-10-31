package org.ozwillo.dcimporter.repository

import org.ozwillo.dcimporter.model.AccessRequestState
import org.ozwillo.dcimporter.model.DataAccessRequest
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import reactor.core.publisher.Flux

interface DataAccessRequestRepository: ReactiveMongoRepository<DataAccessRequest, String>{

    fun findByState(state: AccessRequestState): Flux<DataAccessRequest>
}