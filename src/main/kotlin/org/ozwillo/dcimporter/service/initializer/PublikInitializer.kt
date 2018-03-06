package org.ozwillo.dcimporter.service.initializer

import org.ozwillo.dcimporter.model.publik.PublikConfiguration
import org.ozwillo.dcimporter.repository.PublikConfigurationRepository
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct

@Component
@Profile("dev")
class PublikInitializer(private val publikConfigurationRepository: PublikConfigurationRepository) {

    @PostConstruct
    fun init() {
        if (publikConfigurationRepository.count().block() == 0L) {
            val publikConfiguration = PublikConfiguration(domain = "demarches-sve.test-demarches.sictiam.fr",
                    organizationName = "SICTIAM", secret = "aSYZexOBIzl8")
            publikConfigurationRepository.save(publikConfiguration).subscribe()
        }
    }
}