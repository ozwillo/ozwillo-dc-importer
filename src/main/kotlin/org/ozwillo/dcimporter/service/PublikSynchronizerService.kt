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
    private val kernelService: KernelService,
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

    private val LOGGER = LoggerFactory.getLogger(javaClass)

    override fun run(vararg args: String) {

        businessAppConfigurationRepository.findByApplicationName(PublikService.name).zipWith<String> {
            kernelService.getAccessToken()
        }.subscribe { appConfWithToken ->
            datacoreService.getResourceFromIRI(
                datacoreProject,
                datacoreModelORG,
                "FR/${appConfWithToken.t1.organizationSiret}",
                appConfWithToken.t2
            ).subscribe {
                val queryParameters = DCQueryParameters(
                    "citizenreq:organization", DCOperator.EQ,
                    DCOrdering.DESCENDING, it.getUri()
                )
                listOf(datacoreModelEM /*, datacoreModelSVE*/).forEach { type ->
                    val result = datacoreService.findResources(datacoreProject, type!!, queryParameters, 0, 1, appConfWithToken.t2).blockFirst()
                    if (result != null) {
                        if (type == datacoreModelEM)
                            publikService.syncPublikForms(appConfWithToken.t1, it, formTypeEM)
                                .subscribe { LOGGER.debug("Synchro finished with $it") }
                        else if (type == datacoreModelSVE)
                            publikService.syncPublikForms(appConfWithToken.t1, it, formTypeSVE)
                                .subscribe { LOGGER.debug("Synchro finished with $it") }
                    }
                }
            }
        }
    }
}
