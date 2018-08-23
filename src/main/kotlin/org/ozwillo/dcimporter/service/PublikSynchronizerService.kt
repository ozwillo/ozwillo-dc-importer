package org.ozwillo.dcimporter.service

import org.ozwillo.dcimporter.model.datacore.DCOperator
import org.ozwillo.dcimporter.model.datacore.DCOrdering
import org.ozwillo.dcimporter.model.datacore.DCQueryParameters
import org.ozwillo.dcimporter.repository.BusinessAppConfigurationRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("!test")
class PublikSynchronizerService(private val datacoreService: DatacoreService,
                                private val publikService: PublikService,
                                private val businessAppConfigurationRepository: BusinessAppConfigurationRepository) : CommandLineRunner {

    @Value("\${publik.datacore.project}")
    private val datacoreProject: String = "datacoreProject"
    @Value("\${publik.datacore.modelEM}")
    private val datacoreModelEM: String? = null
    @Value("\${publik.datacore.modelSVE}")
    private val datacoreModelSVE: String? = null
    @Value("\${publik.datacore.modelORG}")
    private val datacoreModelORG: String = "org:Organization_0"
    @Value("\${publik.formTypeEM}")
    private lateinit var formTypeEM: String
    @Value("\${publik.formTypeSVE}")
    private lateinit var formTypeSVE: String

    override fun run(vararg args: String) {

        businessAppConfigurationRepository.findByApplicationName(PublikService.name).subscribe { publikConfiguration ->

            val dcResource = datacoreService.getResourceFromIRI(datacoreProject, datacoreModelORG, "FR/${publikConfiguration.organizationSiret}", null)
            val queryParameters = DCQueryParameters("citizenreq:organization", DCOperator.EQ,
                    DCOrdering.DESCENDING, dcResource.getUri())

            listOf(datacoreModelEM /*, datacoreModelSVE*/).forEach { type ->
                val result = datacoreService.findResource(datacoreProject, type!!, queryParameters).blockOptional()
                if (result.isPresent && result.get().isEmpty()) {
                    if (type == datacoreModelEM)
                        publikService.syncPublikForms(publikConfiguration, dcResource, formTypeEM)
                                .subscribe { LOGGER.debug("Synchro finished with $it") }
                    else if (type == datacoreModelSVE)
                        publikService.syncPublikForms(publikConfiguration, dcResource, formTypeSVE).block()
                }
            }
        }
    }

    companion object {

        private val LOGGER = LoggerFactory.getLogger(PublikSynchronizerService::class.java)
    }
}