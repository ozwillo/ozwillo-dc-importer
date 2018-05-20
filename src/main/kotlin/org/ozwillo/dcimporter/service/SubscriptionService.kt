package org.ozwillo.dcimporter.service

import org.ozwillo.dcimporter.model.Subscription
import org.ozwillo.dcimporter.model.datacore.DCBusinessResourceLight
import org.ozwillo.dcimporter.repository.SubscriptionRepository
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import javax.annotation.PostConstruct

@Service
class SubscriptionService(private val subscriptionRepository: SubscriptionRepository,
                          private val applicationContext: ApplicationContext) {

    var subscribers: Map<String, Subscriber> = emptyMap()

    @PostConstruct
    fun initSubscribers() {
        applicationContext.getBeansOfType(Subscriber::class.java)
                .forEach { entry -> subscribers.plus(Pair(entry.value.getName(), entry.value)) }

    }

    fun add(subscription: Subscription) = subscriptionRepository.save(subscription)

    // TODO : replug a working notification mechanism
    fun notifyMock(dcModelType: DCModelType, dcResource: DCBusinessResourceLight): Mono<String> {
        return Mono.just("OK")
    }

    fun notify(dcModelType: DCModelType, dcResource: DCBusinessResourceLight): Flux<String> {
        return subscriptionRepository.findByModel(dcModelType)
                .filter { subscription ->
                    dcResource.getValues().getValue(subscription.additionalField) == subscription.additionalValue
                }
                .map { subscription -> subscribers[subscription.subscriberName] }
                .flatMap { subscriber -> (subscriber!!::onNewData)(dcResource) }
    }
}