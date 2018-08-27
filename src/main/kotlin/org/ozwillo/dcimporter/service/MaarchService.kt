package org.ozwillo.dcimporter.service

import org.ozwillo.dcimporter.config.FullLoggingInterceptor
import org.ozwillo.dcimporter.model.BusinessMapping
import org.ozwillo.dcimporter.model.maarch.MaarchFile
import org.ozwillo.dcimporter.model.maarch.MaarchArrayData
import org.ozwillo.dcimporter.model.datacore.DCBusinessResourceLight
import org.ozwillo.dcimporter.model.maarch.MaarchContact
import org.ozwillo.dcimporter.model.maarch.MaarchResource
import org.ozwillo.dcimporter.repository.BusinessAppConfigurationRepository
import org.ozwillo.dcimporter.repository.BusinessMappingRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.math.BigInteger

@Service
class MaarchService(private val businessMappingRepository: BusinessMappingRepository,
                    private val businessAppConfigurationRepository: BusinessAppConfigurationRepository) {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(MaarchService::class.java)
        const val name: String = "maarch"
    }

    @Value("\${publik.datacore.modelEM}")
    private val type ="type"

    fun createCitizenRequest(siret: String, dcResource: DCBusinessResourceLight) {
        LOGGER.debug("Preparing to send resource ${dcResource.getUri()}")
        LOGGER.debug("\tcontaining $dcResource")

        if (dcResource.getValues()["citizenreqem:fileContent"] == null) {
            LOGGER.warn("No file attached to resource, not sending it to Maarch GED")
            return
        }

        val businessAppConfiguration = businessAppConfigurationRepository.findByOrganizationSiretAndApplicationName(siret, MaarchService.name).block()!!
        val maarchFileMetadataList = listOf(
                MaarchArrayData(column = "subject", value = dcResource.getValues()["citizenreq:displayName"]!!.toString()),
                MaarchArrayData(column = "type_id", value = "102"),
                MaarchArrayData(column = "custom_t1", value = dcResource.getUri()))
        val fileFormat = dcResource.getValues()["citizenreqem:fileContentType"].toString().substringAfterLast("/").toUpperCase()
        val maarchFile = MaarchFile(status = "COU",
                collId = "letterbox_coll",
                data = maarchFileMetadataList,
                fileFormat = fileFormat,
                table = "res_letterbox",
                encodedFile = dcResource.getValues()["citizenreqem:fileContent"].toString())

        val restTemplate: RestTemplate = RestTemplateBuilder().basicAuthorization(businessAppConfiguration.login, businessAppConfiguration.password).build()
        restTemplate.interceptors.add(FullLoggingInterceptor())

        val storeResourceResponse =
                restTemplate.postForObject("${businessAppConfiguration.baseUrl}/rest/res", maarchFile, StoreResourceResponse::class.java)
        LOGGER.debug("Got store resource response $storeResourceResponse")

        val businessMapping = BusinessMapping(applicationName = name,
                businessId = storeResourceResponse!!.resId.toString(),
                dcId = dcResource.getUri(), type = type)
        businessMappingRepository.save(businessMapping).subscribe()

        val contact = MaarchContact(lastname = dcResource.getValues()["citizenreqem:familyName"]!!.toString(),
                firstname = dcResource.getValues()["citizenreqem:firstName"]!!.toString(),
                email = dcResource.getValues()["citizenreqem:email"]!!.toString(),
                isCorporatePerson = "N", contactType = 106, contactPurposeId = 3)
        val createContactResponse = restTemplate.postForObject("${businessAppConfiguration.baseUrl}/rest/contacts", contact, CreateContactResponse::class.java)
        LOGGER.debug("Got create contact response $createContactResponse")

        val maarchResourceDataList = listOf(
                MaarchArrayData(column = "exp_contact_id", value = createContactResponse!!.contactId.toString()),
                MaarchArrayData(column = "address_id", value = createContactResponse.addressId.toString()),
                MaarchArrayData(column = "category_id", value = "incoming"))
        val maarchResource = MaarchResource(resId = storeResourceResponse.resId.toString(),
                table = "mlb_coll_ext", data = maarchResourceDataList)
        val storeResourceExtResponse = restTemplate.postForObject("${businessAppConfiguration.baseUrl}/rest/resExt", maarchResource,
                StoreResourceResponse::class.java)
        LOGGER.debug("Got store resource ext response $storeResourceExtResponse")
    }

    data class StoreResourceResponse(val returnCode: Int,
                                     val resId: BigInteger,
                                     val errors: String?)

    data class CreateContactResponse(val returnCode: Int,
                                     val contactId: BigInteger,
                                     val addressId: BigInteger,
                                     val errors: String?)
}