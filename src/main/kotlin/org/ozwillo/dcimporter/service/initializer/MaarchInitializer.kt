package org.ozwillo.dcimporter.service.initializer

import org.ozwillo.dcimporter.model.BusinessAppConfiguration
import org.ozwillo.dcimporter.repository.BusinessAppConfigurationRepository
import org.ozwillo.dcimporter.service.MaarchService
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct

@Component
@Profile("dev")
class MaarchInitializer(private val businessAppConfigurationRepository: BusinessAppConfigurationRepository) {

    @PostConstruct
    fun init() {
        businessAppConfigurationRepository.findByOrganizationSiretAndApplicationName("20003019500115", MaarchService.name)
                .switchIfEmpty(
                        businessAppConfigurationRepository.save(BusinessAppConfiguration(baseUrl = "https://e-courrier.sictiam.fr/8ba7be1e-2844-4673-ba9e-dcbe27323b1e",
                                organizationSiret = "20003019500115", login = "restUser", password = "maarch",
                                applicationName = MaarchService.name))
                ).subscribe()
    }
}