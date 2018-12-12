package org.ozwillo.dcimporter.repository

import org.ozwillo.dcimporter.model.ProcessingStat
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import java.time.LocalDateTime

@Repository
interface ProcessingStatRepository: ReactiveMongoRepository<ProcessingStat, String>{

    fun findByModel(
        model:String
    ): Flux<ProcessingStat>

    fun findByOrganization(
        siret: String
    ): Flux<ProcessingStat>

    fun findByAction(
        action: String
    ): Flux<ProcessingStat>

    fun findByCreationDateAfter(
        date: LocalDateTime
    ): Flux<ProcessingStat>

    fun findByModelAndCreationDateAfter(
        model: String,
        date: LocalDateTime
    ): Flux<ProcessingStat>

    fun findByOrganizationAndCreationDateAfter(
        organization: String,
        date: LocalDateTime
    ): Flux<ProcessingStat>

    fun findByActionAndCreationDateAfter(
        action: String,
        date:LocalDateTime
    ): Flux<ProcessingStat>

    fun findByModelAndOrganizationAndCreationDateAfter(
        model: String,
        organization: String,
        date: LocalDateTime
    ): Flux<ProcessingStat>

    fun findByModelAndActionAndCreationDateAfter(
        model: String,
        action: String,
        date:LocalDateTime
    ): Flux<ProcessingStat>

    fun findByOrganizationAndActionAndCreationDateAfter(
        organization: String,
        action: String,
        date: LocalDateTime
    ):Flux<ProcessingStat>

    fun findByModelAndOrganization(
        model: String,
        organization: String
    ): Flux<ProcessingStat>

    fun findByModelAndAction(
        model: String,
        action: String
    ): Flux<ProcessingStat>

    fun findByModelAndOrganizationAndAction(
        model: String,
        organization: String,
        action: String
    ): Flux<ProcessingStat>

    fun findByOrganizationAndAction(
        organization: String,
        action: String
    ): Flux<ProcessingStat>

    fun findByModelAndOrganizationAndActionAndCreationDateAfter(
        model: String,
        organization: String,
        action: String,
        date: LocalDateTime
    ): Flux<ProcessingStat>
}