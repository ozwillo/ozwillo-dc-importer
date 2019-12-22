package org.ozwillo.dcimporter.repository

import java.time.LocalDateTime
import org.ozwillo.dcimporter.model.ProcessingStat
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux

@Repository
interface ProcessingStatRepository : ReactiveMongoRepository<ProcessingStat, String> {

    fun findByCreationDateAfter(
        date: LocalDateTime
    ): Flux<ProcessingStat>
}
