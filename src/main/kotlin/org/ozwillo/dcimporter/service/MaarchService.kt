package org.ozwillo.dcimporter.service

import org.ozwillo.dcimporter.config.FullLoggingInterceptor
import org.ozwillo.dcimporter.model.BusinessMapping
import org.ozwillo.dcimporter.model.maarch.MaarchFile
import org.ozwillo.dcimporter.model.maarch.MaarchArrayData
import org.ozwillo.dcimporter.model.datacore.DCBusinessResourceLight
import org.ozwillo.dcimporter.model.maarch.MaarchContact
import org.ozwillo.dcimporter.model.maarch.MaarchResource
import org.ozwillo.dcimporter.repository.BusinessMappingRepository
import org.slf4j.LoggerFactory
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import reactor.core.publisher.Mono
import java.math.BigInteger

@Service
class MaarchService(private val businessMappingRepository: BusinessMappingRepository) : Subscriber {

    // TODO : externalize
    private val url = "https://e-courrier.sictiam.fr/8ba7be1e-2844-4673-ba9e-dcbe27323b1e"
    private val user = "restUser"
    private val password = "maarch"

    companion object {
        private val LOGGER = LoggerFactory.getLogger(MaarchService::class.java)
        const val name: String = "Maarch GEC"
    }

    override fun getName(): String = name

    override fun onNewData(dcResource: DCBusinessResourceLight): Mono<String> {
        LOGGER.debug("Preparing to send resource ${dcResource.getUri()}")
        LOGGER.debug("\tcontaining $dcResource")
        val maarchFileMetadataList = listOf(
                MaarchArrayData(column = "subject", value = dcResource.getValues()["citizenreq:displayName"]!!),
                MaarchArrayData(column = "type_id", value = "102"),
                MaarchArrayData(column = "custom_t1", value = dcResource.getUri()))
        val maarchFile = MaarchFile(status = "COU", collId = "letterbox_coll", data = maarchFileMetadataList,
                fileFormat = "pdf", table = "res_letterbox", encodedFile = dcResource.gimmeResourceFile().base64content)
        LOGGER.debug("Generated Maarch file ${maarchFile.data[0].value}")

        val restTemplate: RestTemplate = RestTemplateBuilder().basicAuthorization(user, password).build()
        restTemplate.interceptors.add(FullLoggingInterceptor())

        val storeResourceResponse =
                restTemplate.postForObject("$url/rest/res", maarchFile, StoreResourceResponse::class.java)
        LOGGER.debug("Got store resource response $storeResourceResponse")

        val businessMapping = BusinessMapping(applicationName = getName(),
                businessId = storeResourceResponse!!.resId.toString(),
                dcId = dcResource.getUri())
        val savedBusinessMapping = businessMappingRepository.save(businessMapping).block()!!

        val contact = MaarchContact(lastname = dcResource.getValues()["citizenreqem:familyName"]!!,
                firstname = dcResource.getValues()["citizenreqem:firstName"]!!,
                email = dcResource.getValues()["citizenreqem:email"]!!,
                isCorporatePerson = "N", contactType = 106, contactPurposeId = 3)
        val createContactResponse = restTemplate.postForObject("$url/rest/contacts", contact, CreateContactResponse::class.java)
        LOGGER.debug("Got create contact response $createContactResponse")

        val maarchResourceDataList = listOf(
                MaarchArrayData(column = "exp_contact_id", value = createContactResponse!!.contactId.toString()),
                MaarchArrayData(column = "address_id", value = createContactResponse.addressId.toString()),
                MaarchArrayData(column = "category_id", value = "incoming"))
        val maarchResource = MaarchResource(resId = storeResourceResponse.resId.toString(),
                table = "mlb_coll_ext", data = maarchResourceDataList)
        val storeResourceExtResponse = restTemplate.postForObject("$url/rest/resExt", maarchResource,
                StoreResourceResponse::class.java)
        LOGGER.debug("Got store resource ext response $storeResourceExtResponse")

        return Mono.just(dcResource.getUri())
    }

    data class StoreResourceResponse(val returnCode: Int,
                                     val resId: BigInteger,
                                     val errors: String?)

    data class CreateContactResponse(val returnCode: Int,
                                     val contactId: BigInteger,
                                     val addressId: BigInteger,
                                     val errors: String?)
}