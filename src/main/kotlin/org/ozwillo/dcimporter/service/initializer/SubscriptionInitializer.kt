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
        if (subscriptionRepository.count().block() == 0L) {
            val subscriptions = Flux.just(
                    Subscription(model = "citizenreq:elecmeeting_0",
                            organizationSiret = "250601879",
                            additionalField = "citizenreq:workflowStatus", additionalValue = "Terminé",
                            subscriberName = "Maarch GEC"),
                    Subscription(model = "citizenreq:elecmeeting_0",
                            organizationSiret = "250601879",
                            additionalField = "citizenreq:workflowStatus", additionalValue = "Sent Citizen",
                            subscriberName = "Publik")
            )

            subscriptionRepository.saveAll(subscriptions).subscribe()
        }
    }
}