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
class PublikSynchronizerService(
    private val datacoreService: DatacoreService,
    private val publikService: PublikService,
    private val businessAppConfigurationRepository: BusinessAppConfigurationRepository
) : CommandLineRunner {

    @Value("\${datacore.model.project}")
    private val datacoreProject: String = "datacoreProject"
    @Value("\${datacore.model.modelEM}")
    private val datacoreModelEM: String? = null
    @Value("\${datacore.model.modelSVE}")
    private val datacoreModelSVE: String? = null
    @Value("\${datacore.model.modelORG}")
    private val datacoreModelORG: String = "org:Organization_0"
    @Value("\${publik.formTypeEM}")
    private lateinit var formTypeEM: String
    @Value("\${publik.formTypeSVE}")
    private lateinit var formTypeSVE: String

    override fun run(vararg args: String) {

        businessAppConfigurationRepository.findByApplicationName(PublikService.name).subscribe { publikConfiguration ->

            datacoreService.getResourceFromIRI(
                datacoreProject,
                datacoreModelORG,
                "FR/${publikConfiguration.organizationSiret}",
                null
            ).subscribe {
                val queryParameters = DCQueryParameters(
                    "citizenreq:organization", DCOperator.EQ,
                    DCOrdering.DESCENDING, it.getUri()
                )
                listOf(datacoreModelEM /*, datacoreModelSVE*/).forEach { type ->
                    val result = datacoreService.findResource(datacoreProject, type!!, queryParameters).blockOptional()
                    if (result.isPresent && result.get().isEmpty()) {
                        if (type == datacoreModelEM)
                            publikService.syncPublikForms(publikConfiguration, it, formTypeEM)
                                .subscribe { LOGGER.debug("Synchro finished with $it") }
                        else if (type == datacoreModelSVE)
                            publikService.syncPublikForms(publikConfiguration, it, formTypeSVE).block()
                    }
                }
            }
        }
    }

    companion object {

        private val LOGGER = LoggerFactory.getLogger(PublikSynchronizerService::class.java)
    }
}
