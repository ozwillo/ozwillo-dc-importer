package org.ozwillo.dcimporter.service

import org.ozwillo.dcimporter.config.FullLoggingInterceptor
import org.ozwillo.dcimporter.model.BusinessMapping
import org.ozwillo.dcimporter.model.datacore.DCBusinessResourceLight
import org.ozwillo.dcimporter.model.egm.EgmArrayData
import org.ozwillo.dcimporter.model.egm.EgmContact
import org.ozwillo.dcimporter.model.egm.EgmFile
import org.ozwillo.dcimporter.model.egm.EgmResource
import org.ozwillo.dcimporter.repository.BusinessAppConfigurationRepository
import org.ozwillo.dcimporter.repository.BusinessMappingRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.web.client.RestTemplate
import java.math.BigInteger

class EgmService (
    private val businessMappingRepository: BusinessMappingRepository,
    private val businessAppConfigurationRepository: BusinessAppConfigurationRepository
) {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(EgmService::class.java)
        const val name: String = "egm"
    }

    @Value("\${datacore.model.modelEM}")
    private val type = "type"

    fun createCitizenRequest(siret: String, dcResource: DCBusinessResourceLight) {
        LOGGER.debug("Preparing to send resource ${dcResource.getUri()}")
        LOGGER.debug("\tcontaining $dcResource")

        if (dcResource.getValues()["citizenreqem:fileContent"] == null) {
            LOGGER.warn("No file attached to resource, not sending it to EGM GED")
            return
        }

        val businessAppConfiguration = businessAppConfigurationRepository.findByOrganizationSiretAndApplicationName(
            siret,
            EgmService.name
        ).block()!!
        val egmFileMetadataList = listOf(
            EgmArrayData(column = "subject", value = dcResource.getValues()["citizenreq:displayName"]!!.toString()),
            EgmArrayData(column = "type_id", value = "102"),
            EgmArrayData(column = "custom_t1", value = dcResource.getUri())
        )
        val fileFormat =
            dcResource.getValues()["citizenreqem:fileContentType"].toString().substringAfterLast("/").toUpperCase()
        val egmFile = EgmFile(
            status = "COU",
            collId = "letterbox_coll",
            data = egmFileMetadataList,
            fileFormat = fileFormat,
            table = "res_letterbox",
            encodedFile = dcResource.getValues()["citizenreqem:fileContent"].toString()
        )

        val restTemplate: RestTemplate =
            RestTemplateBuilder().basicAuthorization(businessAppConfiguration.login, businessAppConfiguration.password)
                .build()
        restTemplate.interceptors.add(FullLoggingInterceptor())

        val storeResourceResponse =
            restTemplate.postForObject(
                "${businessAppConfiguration.baseUrl}/rest/res",
                egmFile,
                StoreResourceResponse::class.java
            )
        LOGGER.debug("Got store resource response $storeResourceResponse")

        val businessMapping = BusinessMapping(
            applicationName = name,
            businessId = storeResourceResponse!!.resId.toString(),
            dcId = dcResource.getUri(), type = type
        )
        businessMappingRepository.save(businessMapping).subscribe()

        val contact = EgmContact(
            lastname = dcResource.getValues()["citizenreqem:familyName"]!!.toString(),
            firstname = dcResource.getValues()["citizenreqem:firstName"]!!.toString(),
            email = dcResource.getValues()["citizenreqem:email"]!!.toString(),
            isCorporatePerson = "N", contactType = 106, contactPurposeId = 3
        )
        val createContactResponse = restTemplate.postForObject(
            "${businessAppConfiguration.baseUrl}/rest/contacts",
            contact,
            CreateContactResponse::class.java
        )
        LOGGER.debug("Got create contact response $createContactResponse")

        val egmResourceDataList = listOf(
            EgmArrayData(column = "exp_contact_id", value = createContactResponse!!.contactId.toString()),
            EgmArrayData(column = "address_id", value = createContactResponse.addressId.toString()),
            EgmArrayData(column = "category_id", value = "incoming")
        )
        val egmResource = EgmResource(
            resId = storeResourceResponse.resId.toString(),
            table = "mlb_coll_ext", data = egmResourceDataList
        )
        val storeResourceExtResponse = restTemplate.postForObject(
            "${businessAppConfiguration.baseUrl}/rest/resExt", egmResource,
            StoreResourceResponse::class.java
        )
        LOGGER.debug("Got store resource ext response $storeResourceExtResponse")
    }

    data class StoreResourceResponse(
        val returnCode: Int,
        val resId: BigInteger,
        val errors: String?
    )

    data class CreateContactResponse(
        val returnCode: Int,
        val contactId: BigInteger,
        val addressId: BigInteger,
        val errors: String?
    )
}