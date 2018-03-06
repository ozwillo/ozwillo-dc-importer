package org.ozwillo.dcimporter.repository

import org.ozwillo.dcimporter.model.Subscription
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux

@Repository
interface SubscriptionRepository : ReactiveCrudRepository<Subscription, String> {
    fun findByModel(model: String): Flux<Subscription>
    fun findByModelAndAdditionalField(model: String, additionalField: String): Flux<Subscription>
}