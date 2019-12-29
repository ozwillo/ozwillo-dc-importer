package org.ozwillo.dcimporter.repository

import java.util.*
import org.ozwillo.dcimporter.model.Subscription
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository

@Repository
interface SubscriptionRepository : ReactiveMongoRepository<Subscription, UUID>
