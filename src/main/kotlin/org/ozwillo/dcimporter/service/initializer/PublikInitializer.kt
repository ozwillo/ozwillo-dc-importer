package org.ozwillo.dcimporter.service.initializer

import org.ozwillo.dcimporter.model.BusinessAppConfiguration
import org.ozwillo.dcimporter.repository.BusinessAppConfigurationRepository
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct

@Component
@Profile("dev")
class PublikInitializer(private val businessAppConfigurationRepository: BusinessAppConfigurationRepository) {

    @PostConstruct
    fun init() {
        businessAppConfigurationRepository.findByDomainAndApplicationName("demarches-sve.test-demarches.sictiam.fr", "Publik")
                .switchIfEmpty(
                        businessAppConfigurationRepository.save(BusinessAppConfiguration(domain = "demarches-sve.test-demarches.sictiam.fr",
                                organizationName = "SICTIAM", secret = "aSYZexOBIzl8", applicationName = "Publik"))
                ).subscribe()
    }
}