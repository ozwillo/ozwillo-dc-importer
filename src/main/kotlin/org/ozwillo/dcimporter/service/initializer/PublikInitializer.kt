package org.ozwillo.dcimporter.service.initializer

import org.ozwillo.dcimporter.model.BusinessAppConfiguration
import org.ozwillo.dcimporter.repository.BusinessAppConfigurationRepository
import org.ozwillo.dcimporter.service.PublikService
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct

@Component
@Profile("dev")
class PublikInitializer(private val businessAppConfigurationRepository: BusinessAppConfigurationRepository) {

    @PostConstruct
    fun init() {
        businessAppConfigurationRepository.findByOrganizationSiretAndApplicationName("20003019500115", PublikService.name)
                .switchIfEmpty(
                        businessAppConfigurationRepository.save(BusinessAppConfiguration(baseUrl = "https://demarches-sve.test-demarches.sictiam.fr",
                                organizationSiret = "20003019500115", secretOrToken = "aSYZexOBIzl8",
                                applicationName = PublikService.name))
                ).subscribe()
    }
}