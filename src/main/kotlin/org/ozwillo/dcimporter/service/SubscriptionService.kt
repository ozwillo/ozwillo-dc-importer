package org.ozwillo.dcimporter.service

import java.time.LocalDateTime
import org.ozwillo.dcimporter.model.NotificationLog
import org.ozwillo.dcimporter.model.Subscription
import org.ozwillo.dcimporter.repository.NotificationLogRepository
import org.ozwillo.dcimporter.repository.SubscriptionRepository
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.find
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.elemMatch
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*

@Service
class SubscriptionService(
    private val subscriptionRepository: SubscriptionRepository,
    private val notificationLogRepository: NotificationLogRepository,
    private val mongoTemplate: ReactiveMongoTemplate
) {

    fun findAll(): Flux<Subscription> = subscriptionRepository.findAll()

    fun findForEventType(eventType: String): Flux<Subscription> =
        mongoTemplate.find(
            Query(Subscription::events elemMatch Criteria().`in`(eventType))
        )

    fun notifyForEventType(eventType: String): Flux<NotificationLog> {
        return findForEventType(eventType)
            .flatMap { subscription ->
                callSubscriber(subscription, eventType)
            }
            .map {
                NotificationLog(subscriptionId = it.first.uuid,
                    eventType = eventType, notificationDate = LocalDateTime.now(),
                    result = it.second, errorMessage = it.third)
            }
            .flatMap {
                notificationLogRepository.save(it)
            }
            .log()
    }

    fun callSubscriber(subscription: Subscription, eventType: String): Mono<Triple<Subscription, Int, String?>> {
        return WebClient.create(subscription.url)
            .post()
            .header("X-Ozwillo-Event", eventType)
            .header("X-Ozwillo-Delivery", UUID.randomUUID().toString())
            .retrieve()
            .toBodilessEntity()
            .map {
                Triple<Subscription, Int, String?>(subscription, it.statusCodeValue, null)
            }
            .onErrorResume {
                val error = it as WebClientResponseException
                Mono.just(Triple(subscription, error.rawStatusCode, error.responseBodyAsString))
            }
    }
}
