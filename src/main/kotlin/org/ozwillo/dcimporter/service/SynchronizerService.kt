package org.ozwillo.dcimporter.service

import java.util.Arrays

import org.oasis_eu.spring.datacore.DatacoreClient
import org.oasis_eu.spring.datacore.model.DCOperator
import org.oasis_eu.spring.datacore.model.DCQueryParameters
import org.ozwillo.dcimporter.config.Prop
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component

@Component
class SynchronizerService(private val systemUserService: SystemUserService,
                          private val datacoreClient: DatacoreClient,
                          private val publikService: PublikService,
                          private val props: Prop) : CommandLineRunner {

    @Value("\${publik.datacore.project}")
    private val datacoreProject: String? = null
    @Value("\${publik.datacore.modelEM}")
    private val datacoreModelEM: String? = null
    @Value("\${publik.datacore.modelSVE}")
    private val datacoreModelSVE: String? = null
    @Value("\${publik.formTypeEM}")
    private lateinit var formTypeEM: String
    @Value("\${publik.formTypeSVE}")
    private lateinit var formTypeSVE: String

    override fun run(vararg args: String) {
        systemUserService.runAs(Runnable {

            props.instance.forEach { instance ->

                val dcOrg = publikService.getDCOrganization(instance["organization"])
                if (!dcOrg.isPresent) {
                    LOGGER.error("Unable to find organization {}", instance["organization"])
                } else {
                    val queryParameters = DCQueryParameters("citizenreq:organization", DCOperator.EQ, dcOrg.get().uri)

                    Arrays.asList<String>(datacoreModelEM, datacoreModelSVE).forEach { type ->
                        if (datacoreClient.findResources(datacoreProject, type, queryParameters, 0, 1).isEmpty())
                            try {
                                if (type == datacoreModelEM)
                                    publikService.syncPublikForms(instance["baseUrl"]!!, dcOrg.get(), formTypeEM)
                                else if (type == datacoreModelSVE)
                                    publikService.syncPublikForms(instance["baseUrl"]!!, dcOrg.get(), formTypeSVE)
                                LOGGER.debug("Requests successfully synchronized")
                            } catch (e: Exception) {
                                LOGGER.warn("Unable to synchronize past requests of type {} for {}",
                                        type, instance["organization"], e)
                            }
                        else
                            LOGGER.debug("Requests of type {} are already synchronized for {}", type, dcOrg.get().uri)
                    }
                }
            }
        })
    }

    companion object {

        private val LOGGER = LoggerFactory.getLogger(SynchronizerService::class.java)
    }
}