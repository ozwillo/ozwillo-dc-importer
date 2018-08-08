package org.ozwillo.dcimporter.service.initializer

import org.ozwillo.dcimporter.model.BusinessAppConfiguration
import org.ozwillo.dcimporter.repository.BusinessAppConfigurationRepository
import org.ozwillo.dcimporter.service.MarcheSecuriseService
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct

@Component
@Profile("dev")
class MarcheSecuriseInitializer(private val businessAppConfigurationRepository: BusinessAppConfigurationRepository) {

    @PostConstruct
    fun init() {
        businessAppConfigurationRepository.findByOrganizationSiretAndApplicationName("20003019500115", MarcheSecuriseService.name)
                .switchIfEmpty(
                        businessAppConfigurationRepository.save(BusinessAppConfiguration(baseUrl = "https://marches-securises.fr",
                                organizationSiret = "20003019500115",
                                instanceId = "identifiant PA", login = "login", password = "password",
                                applicationName = MarcheSecuriseService.name))
                ).subscribe()
    }
}