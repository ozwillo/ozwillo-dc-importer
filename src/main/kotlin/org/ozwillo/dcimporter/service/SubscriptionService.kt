package org.ozwillo.dcimporter.service

import org.oasis_eu.spring.datacore.model.DCResult
import org.ozwillo.dcimporter.model.Subscription
import org.ozwillo.dcimporter.repository.SubscriptionRepository
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import javax.annotation.PostConstruct
import kotlin.reflect.full.memberProperties

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

    fun notify(dcResult: DCResult): Flux<String> {
        return subscriptionRepository.findByModel(dcResult.resource.type)
                .filter { subscription ->
                    val properties = dcResult.resource.javaClass.kotlin.memberProperties
                    val property = properties.find { kProperty1 -> kProperty1.name == subscription.additionalField }
                    val propertyValue = property!!.getter.call(dcResult.resource)
                    subscription.additionalValue == propertyValue
                }
                .map { subscription -> subscribers[subscription.subscriberName] }
                .flatMap { subscriber -> (subscriber!!::onNewData)(dcResult.resource) }
    }
}