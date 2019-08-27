package org.ozwillo.dcimporter.repository

import org.ozwillo.dcimporter.model.ProcessingStat
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import java.time.LocalDateTime

@Repository
interface ProcessingStatRepository : ReactiveMongoRepository<ProcessingStat, String> {

    fun findByCreationDateAfter(
        date: LocalDateTime
    ): Flux<ProcessingStat>
}