package org.ozwillo.dcimporter.util

import org.ozwillo.dcimporter.model.Subscription
import org.ozwillo.dcimporter.repository.SubscriptionRepository
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Profile("dev")
@Component
class DataBootstrapper(
    private val subscriptionRepository: SubscriptionRepository
) : CommandLineRunner {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun run(vararg args: String?) {
        val subscription1 = Subscription(applicationName = "app1", url = "http://localhost:8080/api/notification/dummy",
            events = listOf("grant_0.grant:assocation_0.create", "grant_0.grant:assocation_0.update", "org_1.org:Organization_0.create"))
        val subscription2 = Subscription(applicationName = "app2", url = "http://localhost:8080/api/notification/dummy",
            events = listOf("grant_0.grant:assocation_0.create", "grant_0.grant:assocation_0.delete"))

        subscriptionRepository.count()
            .filter {
                it == 0L
            }
            .flatMapMany {
                subscriptionRepository.saveAll(listOf(subscription1, subscription2))
            }.subscribe {
                logger.debug("Created subscription for app ${it.applicationName}")
            }
    }
}
