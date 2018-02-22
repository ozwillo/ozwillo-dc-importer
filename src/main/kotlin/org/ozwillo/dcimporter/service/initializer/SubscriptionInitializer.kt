package org.ozwillo.dcimporter.service.initializer

import org.ozwillo.dcimporter.model.Subscription
import org.ozwillo.dcimporter.repository.SubscriptionRepository
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import javax.annotation.PostConstruct

@Component
@Profile("dev")
class SubscriptionInitializer(private val subscriptionRepository: SubscriptionRepository) {

    @PostConstruct
    fun init() {
        val subscriptions = Flux.just(
                Subscription(model = "citizenreq:elecmeeting_0",
                        organizationSiret = "25060187900043",
                        additionalField = "workflowStatus", additionalValue = "Accepted",
                        subscriberName = "Maarch GEC"),
                Subscription(model = "citizenreq:elecmeeting_0",
                        organizationSiret = "25060187900043",
                        additionalField = "workflowStatus", additionalValue = "Sent Citizen",
                        subscriberName = "Publik")
        )

        subscriptionRepository.insert(subscriptions).subscribe()
    }
}