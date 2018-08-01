package org.ozwillo.dcimporter.service

import com.fasterxml.jackson.annotation.JsonProperty
import org.apache.commons.lang3.RandomStringUtils
import org.ozwillo.dcimporter.config.FullLoggingInterceptor
import org.ozwillo.dcimporter.model.BusinessMapping
import org.ozwillo.dcimporter.model.datacore.*
import org.ozwillo.dcimporter.model.publik.*
import org.ozwillo.dcimporter.repository.BusinessMappingRepository
import org.ozwillo.dcimporter.repository.PublikConfigurationRepository
import org.ozwillo.dcimporter.util.hmac
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.RequestEntity
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.util.DefaultUriBuilderFactory
import reactor.core.publisher.Mono
import java.net.*
import java.text.SimpleDateFormat
import java.time.Duration
import java.util.*

// TODO : needs a lot of cleanup and refactoring !!!!
@Service
class PublikService(private val datacoreService: DatacoreService,
                    private val publikConfigurationRepository: PublikConfigurationRepository,
                    private val businessMappingRepository: BusinessMappingRepository) {

    @Value("\${publik.formTypeEM}")
    private val formTypeEM: String? = null
    @Value("\${publik.formTypeSVE}")
    private val formTypeSVE: String? = null
    @Value("\${publik.algo}")
    private val algo: String? = null
    @Value("\${publik.orig}")
    private val orig: String = "ozwillo-dcimporter"
    @Value("\${publik.datacore.project}")
    private val datacoreProject: String = "datacoreProject"
    @Value("\${publik.datacore.modelEM}")
    private val datacoreModelEM: String? = null
    @Value("\${publik.datacore.modelSVE}")
    private val datacoreModelSVE: String? = null
    @Value("\${publik.datacore.modelUser}")
    private val datacoreModelUser: String = "citizenreq:user_0"
    @Value("\${datacore.baseUri}")
    private val datacoreBaseUri: String? = null

    private fun getForm(url: String, secret: String): FormModel? {

        val signedQuery = signQuery("email=admin@ozwillo-dev.eu&", secret)
        LOGGER.debug("Getting Publik form at URL $url?$signedQuery")

        val uriBuilderFactory = DefaultUriBuilderFactory()
        uriBuilderFactory.encodingMode = DefaultUriBuilderFactory.EncodingMode.NONE
        val clientV2 = WebClient.builder()
                .uriBuilderFactory(uriBuilderFactory)
                .build()
        return clientV2.get()
                .uri("$url?$signedQuery")
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError,
                        { response -> response.bodyToMono(PublikResponse::class.java).map {
                            RuntimeException(it.toString())
                        } })
                .bodyToMono(FormModel::class.java)
                .block(Duration.ofSeconds(10))
    }

    fun syncPublikForms(publikConfiguration: PublikConfiguration, dcOrganization: DCResourceLight, formType: String): Mono<DCResult> {

        val signedQuery = signQuery("email=admin@ozwillo-dev.eu&", publikConfiguration.secret)

        LOGGER.debug("Getting Publik form list at URL https://${publikConfiguration.domain}/api/forms/$formType/list?$signedQuery")

        val uriBuilderFactory = DefaultUriBuilderFactory()
        uriBuilderFactory.encodingMode = DefaultUriBuilderFactory.EncodingMode.NONE
        val clientV2 = WebClient.builder()
                .uriBuilderFactory(uriBuilderFactory)
                .build()
        return clientV2.get()
                .uri("https://${publikConfiguration.domain}/api/forms/$formType/list?$signedQuery")
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError,
                        { response -> response.bodyToMono(PublikResponse::class.java).map {
                            RuntimeException(it.toString())
                        } })
                .bodyToFlux(ListFormsModel::class.java)
                .map { getForm(formatUrl(it.url), publikConfiguration.secret) }
                .map { convertToDCResource(dcOrganization, it!!) }
                .flatMap { datacoreService.saveResource(datacoreProject, it.first, it.second, null) }
                .count()
                .map { DCResult(HttpStatus.OK) }
    }

    data class PublikResponse(@JsonProperty("err_class") val errClass: String,
                         @JsonProperty("err_desc") val errDesc: String,
                         @JsonProperty("err") val err: Int)

    fun formToDCResource(form: FormModel): Pair<DCModelType, DCBusinessResourceLight> {

        LOGGER.debug("Got form from Publik : $form")
        LOGGER.debug("Form has URL ${form.url}")
        val uri = URI(form.url)
        val publikConfiguration = publikConfigurationRepository.findByDomain(uri.host).block()!!
        val orgResource = datacoreService.getDCOrganization(publikConfiguration.organizationName).block()!!

        val result: Pair<DCModelType, DCBusinessResourceLight> = convertToDCResource(orgResource, form)

        val businessMapping = BusinessMapping(applicationName = "Publik", businessId = form.url,
                dcId = result.second.getUri(), type = datacoreProject)
        val savedBusinessMapping = businessMappingRepository.save(businessMapping).block()!!

        return result
//        return publikConfigurationRepository.findByDomain(uri.host).flatMap {
//            datacoreService.getDCOrganization(it.organizationName).map { orgResource ->
//                convertToDCResource(orgResource, form)
//            }
//        }
    }

    private fun convertToDCResource(dcOrganization: DCResourceLight, form: FormModel): Pair<DCModelType, DCBusinessResourceLight> {

        val userUri = getOrCreateUser(form).block()!!

        val type = if (isEMrequest(form)) datacoreModelEM else datacoreModelSVE
        val dcResource = DCResource(id = dcOrganization.getIri() + "/" + form.display_id, type = type!!)

        dcResource.baseUri = datacoreBaseUri
        dcResource.iri = dcOrganization.getIri() + "/" + form.display_id

        val dcFormResource = DCBusinessResourceLight(dcResource.getUri())

        dcFormResource.setStringValue("citizenreq:displayId", form.display_id)
        dcFormResource.setStringValue("citizenreq:lastUpdateTime", form.last_update_time)
        dcFormResource.setStringValue("citizenreq:displayName", form.display_name)
        dcFormResource.setStringValue("citizenreq:submissionChannel", form.submission.channel)
        dcFormResource.setStringValue("citizenreq:submissionBackoffice", form.submission.backoffice.toString())
        dcFormResource.setStringValue("citizenreq:url", form.url)
        dcFormResource.setStringValue("citizenreq:receiptTime", form.receipt_time)
        dcFormResource.setStringValue("citizenreq:workflowStatus", form.workflowStatus.orEmpty())

        dcFormResource.setStringValue("citizenreq:criticalityLevel", form.criticality_level.toString())
        dcFormResource.setStringValue("citizenreq:id", form.id)
        dcFormResource.setStringValue("citizenreq:organization", dcOrganization.getUri())

        // TODO shouldn't user be instead the guy who issued the request ?
        // Currently it is the agent who triggered the status change
        dcFormResource.setStringValue("citizenreq:user", userUri)

        if (isEMrequest(form)) {
            convertToDCResourceEM(dcFormResource, form)
        } else if (isSVErequest(form)) {
            convertToDCResourceSVE(dcFormResource, form)
        }

        return Pair(type, dcFormResource)
    }

    private fun convertToDCResourceEM(dcResource: DCBusinessResourceLight, form: FormModel): DCBusinessResourceLight {

        dcResource.setStringValue("citizenreqem:familyName", form.fields["nom"].toString())
        dcResource.setStringValue("citizenreqem:firstName", form.fields["prenom"].toString())
        dcResource.setStringValue("citizenreqem:phone", form.fields["telephone"].toString())
        dcResource.setStringValue("citizenreqem:email", form.fields["email"].toString())
        if (form.fields["detail"] != null)
            dcResource.setStringValue("citizenreqem:detail", form.fields["detail"].toString())
        dcResource.setStringValue("citizenreqem:objectSummary", form.fields["objet_raw"].toString())
        dcResource.setStringValue("citizenreqem:objectDetail", form.fields["objet"].toString())
        if (form.fields["date_souhaitee"] != null)
            dcResource.setStringValue("citizenreqem:desiredDate", form.fields["date_souhaitee"].toString())

        if (form.fields["courrier"] != null) {
            val courrierFieds: Map<String, Any> = form.fields["courrier"] as Map<String, Any>
            val base64content = courrierFieds["content"].toString()
            val contentType = courrierFieds["content_type"].toString()
            val filename = courrierFieds["filename"].toString()
            val resourceFile = DCBusinessResourceFile(base64content = base64content, contentType = contentType, filename = filename)
            dcResource.addResourceFile(resourceFile)
        }

        return dcResource
    }

    private fun convertToDCResourceSVE(dcResource: DCBusinessResourceLight, form: FormModel): DCBusinessResourceLight {

        dcResource.setStringValue("citizenreqsve:title", form.fields["civilite"].toString())
        dcResource.setStringValue("citizenreqsve:familyName", form.fields["nom"].toString())
        dcResource.setStringValue("citizenreqsve:firstName", form.fields["prenoms"].toString())
        dcResource.setStringValue("citizenreqsve:email", form.fields["email"].toString())
        dcResource.setStringValue("citizenreqsve:streetAddress", form.fields["voie"].toString())
        dcResource.setStringValue("citizenreqsve:zipCode", form.fields["code_postal"].toString())
        dcResource.setStringValue("citizenreqsve:city", form.fields["commune"].toString())
        dcResource.setStringValue("citizenreqsve:entityType", form.fields["entite_raw"].toString())
        dcResource.setStringValue("citizenreqsve:object", form.fields["objet"].toString())
        dcResource.setStringValue("citizenreqsve:message", form.fields["message"].toString())

        val doc = form.fields["doc"] as HashMap<String, String>
        dcResource.setStringValue("citizenreqsve:docContent", doc["content"].orEmpty())
        dcResource.setStringValue("citizenreqsve:docFieldId", doc["field_id"].orEmpty())
        dcResource.setStringValue("citizenreqsve:docContentType", doc["content_type"].orEmpty())
        dcResource.setStringValue("citizenreqsve:docFileName", doc["filename"].orEmpty())

        if (form.fields["siret"] != null)
            dcResource.setStringValue("citizenreqsve:siret", form.fields["siret"].toString())
        if (form.fields["siret_entreprise"] != null)
            dcResource.setStringValue("citizenreqsve:siret", form.fields["siret_entreprise"].toString())
        if (form.fields["rna"] != null)
            dcResource.setStringValue("citizenreqsve:rna", form.fields["rna"].toString())
        if (form.fields["nom_entite_entreprise"] != null)
            dcResource.setStringValue("citizenreqsve:legalName", form.fields["nom_entite_entreprise"].toString())
        if (form.fields["nom_entite"] != null)
            dcResource.setStringValue("citizenreqsve:entityName", form.fields["nom_entite"].toString())
        return dcResource
    }

    private fun getOrCreateUser(form: FormModel): Mono<String> {

        // for now, let's say agent_sictiam is the universal fallback user
        val nameId = if (form.user == null) "5c977a7f1d444fa1ab0f777325fdda93" else form.user.nameID[0]

        val queryParametersOrg = DCQueryParameters("citizenrequser:nameID", DCOperator.EQ, DCOrdering.DESCENDING,
                nameId)
        val listUsers = datacoreService.findResource(datacoreProject, datacoreModelUser, queryParametersOrg).block()!!

        if (!listUsers.isEmpty())
            return Mono.just(listUsers[0].getUri())
        else
            return createUser(form.user!!)
    }

    private fun createUser(user: User): Mono<String> {
        val dcResource = DCResource(id = user.nameID[0], type = datacoreModelUser)
        dcResource.baseUri = datacoreBaseUri
        dcResource.iri = user.nameID[0]
        val dcUserResource = DCBusinessResourceLight(dcResource.getUri())
        dcUserResource.setStringValue("citizenrequser:email", user.email)
        dcUserResource.setStringValue("citizenrequser:nameID", user.nameID[0])
        dcUserResource.setStringValue("citizenrequser:userId", user.id.toString())
        dcUserResource.setStringValue("citizenrequser:name", user.name)
        return datacoreService.saveResource(datacoreProject, datacoreModelUser, dcUserResource, null).map { saveResult ->
            (saveResult as DCResultSingle).resource.getUri()
        }
    }

    data class PublikStatusResponse(val url: String?, val err: Int?)

    fun changeStatus(publikId: String): PublikStatusResponse {
        LOGGER.debug("Changing status of request $publikId")

        //val uri = URI(publikId)
        //val publikConfiguration = publikConfigurationRepository.findByDomain(uri.host).block()!!

        val signedQuery = signQuery("email=admin@ozwillo-dev.eu&", "aSYZexOBIzl8")
        LOGGER.debug("Changing status of request at URL ${publikId}jump/trigger/close?$signedQuery")

        val restTemplate = RestTemplate()
        restTemplate.interceptors.add(FullLoggingInterceptor())
        val headers = LinkedMultiValueMap<String, String>()
        headers.set("Accept", "application/json")
        val uri = "${publikId}jump/trigger/close?$signedQuery"
        val request = RequestEntity<Any>(null, headers, HttpMethod.POST, URI(uri))

        try {
            val response = restTemplate.exchange(request, PublikStatusResponse::class.java)
            LOGGER.debug("Got Publik response for status change $response")
            return response.body!!
        } catch (e: HttpClientErrorException) {
            // TODO : temp hack while not handling existing resource yet
            LOGGER.error("Publik returned an error", e)
            return PublikStatusResponse(err = -1, url = "")
        }
    }

    private fun signQuery(query: String, secret: String): String {

        val tz = TimeZone.getTimeZone("UTC")
        val df = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
        df.timeZone = tz
        val thisMoment = df.format(Date())

        val nonce = RandomStringUtils.random(64, true, true)

        val fullEncodedQuery = query + "algo=" + this.algo +
                "&timestamp=" + URLEncoder.encode(thisMoment, "UTF-8") +
                "&nonce=" + URLEncoder.encode(nonce, "UTF-8") +
                "&orig=" + URLEncoder.encode(this.orig, "UTF-8")
        val signature = fullEncodedQuery.hmac("HmacSHA256", secret)
        return fullEncodedQuery + "&signature=" + URLEncoder.encode(signature, "UTF-8")
    }

    @Throws(MalformedURLException::class)
    private fun formatUrl(url_base: String): String {

        LOGGER.debug("Formatting URL $url_base")
        val parsedUrl = URL(url_base)
        return "https://" + parsedUrl.host + "/api/forms" + parsedUrl.path
    }

    private fun isEMrequest(form: FormModel): Boolean {
        return form.id.contains(this.formTypeEM!!)
    }

    private fun isSVErequest(form: FormModel): Boolean {
        return form.id.contains(this.formTypeSVE!!)
    }

    companion object {

        private val LOGGER = LoggerFactory.getLogger(PublikService::class.java)
        const val name: String = "Publik"
    }

}