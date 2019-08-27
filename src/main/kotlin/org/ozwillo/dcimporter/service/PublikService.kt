package org.ozwillo.dcimporter.service

import com.fasterxml.jackson.annotation.JsonProperty
import org.apache.commons.lang3.RandomStringUtils
import org.ozwillo.dcimporter.config.DatacoreProperties
import org.ozwillo.dcimporter.model.BusinessAppConfiguration
import org.ozwillo.dcimporter.model.BusinessMapping
import org.ozwillo.dcimporter.model.datacore.*
import org.ozwillo.dcimporter.model.publik.FormModel
import org.ozwillo.dcimporter.model.publik.ListFormsModel
import org.ozwillo.dcimporter.model.publik.User
import org.ozwillo.dcimporter.repository.BusinessAppConfigurationRepository
import org.ozwillo.dcimporter.repository.BusinessMappingRepository
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
import java.net.MalformedURLException
import java.net.URI
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

// TODO : needs a lot of cleanup and refactoring !!!!
@Service
class PublikService(
    private val datacoreService: DatacoreService,
    private val businessAppConfigurationRepository: BusinessAppConfigurationRepository,
    private val businessMappingRepository: BusinessMappingRepository,
    private val datacoreProperties: DatacoreProperties
) {

    @Value("\${publik.formTypeEM}")
    private val formTypeEM: String? = null
    @Value("\${publik.formTypeSVE}")
    private val formTypeSVE: String? = null
    @Value("\${publik.algo}")
    private val algo: String? = null
    @Value("\${publik.orig}")
    private val orig: String = "ozwillo-dcimporter"
    @Value("\${datacore.model.project}")
    private val datacoreProject: String = "datacoreProject"
    @Value("\${datacore.model.modelEM}")
    private val datacoreModelEM: String? = null
    @Value("\${datacore.model.modelSVE}")
    private val datacoreModelSVE: String? = null
    @Value("\${datacore.model.modelUser}")
    private val datacoreModelUser: String = "citizenreq:user_0"
    @Value("\${datacore.model.modelORG}")
    private val datacoreModelORG: String = "org:Organization_0"

    private fun getForm(url: String, secret: String): Mono<FormModel> {

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
                { response ->
                    response.bodyToMono(PublikResponse::class.java).map {
                        RuntimeException(it.toString())
                    }
                })
            .bodyToMono(FormModel::class.java)
    }

    fun syncPublikForms(
        businessAppConfiguration: BusinessAppConfiguration,
        dcOrganization: DCResource,
        formType: String
    ): Mono<HttpStatus> {

        val signedQuery = signQuery("email=admin@ozwillo-dev.eu&", businessAppConfiguration.secretOrToken!!)

        LOGGER.debug(
            "Getting Publik form list at URL ${businessAppConfiguration.baseUrl}/api/forms/$formType/list?$signedQuery")

        val uriBuilderFactory = DefaultUriBuilderFactory()
        uriBuilderFactory.encodingMode = DefaultUriBuilderFactory.EncodingMode.NONE
        val clientV2 = WebClient.builder()
            .uriBuilderFactory(uriBuilderFactory)
            .build()
        return clientV2.get()
            .uri("${businessAppConfiguration.baseUrl}/api/forms/$formType/list?$signedQuery")
            .retrieve()
            .onStatus(HttpStatus::is4xxClientError) { response ->
                response.bodyToMono(PublikResponse::class.java).map {
                    RuntimeException(it.toString())
                }
            }
            .bodyToFlux(ListFormsModel::class.java)
            .flatMap { listFormsModel ->
                getForm(
                    formatUrl(listFormsModel.url),
                    businessAppConfiguration.secretOrToken
                )
            }
            .flatMap { formModel -> convertToDCResource(dcOrganization, formModel) }
            .map { datacoreService.saveResource(datacoreProject, it.first, it.second, null) }
            .count()
            .map { HttpStatus.OK }
    }

    data class PublikResponse(
        @JsonProperty("err_class") val errClass: String,
        @JsonProperty("err_desc") val errDesc: String,
        @JsonProperty("err") val err: Int
    )

    fun formToDCResource(organizationSiret: String, form: FormModel): Mono<Pair<DCModelType, DCResource>> {

        LOGGER.debug("Got form from Publik : $form")

        return datacoreService.getResourceFromIRI(datacoreProject, datacoreModelORG, "FR/$organizationSiret", null)
            .flatMap { convertToDCResource(it, form) }
            .map { result ->
                val businessMapping = BusinessMapping(
                    applicationName = name, businessId = form.url,
                    dcId = result.second.getUri(), type = result.first
                )
                businessMappingRepository.save(businessMapping).subscribe()

                result
        }
    }

    private fun convertToDCResource(dcOrganization: DCResource, form: FormModel): Mono<Pair<DCModelType, DCResource>> {

        return getOrCreateUser(form).map { userUri ->
            val type = if (isEMrequest(form)) datacoreModelEM else datacoreModelSVE

            val dcFormResource = DCResource(datacoreProperties.baseResourceUri(),
                type = type!!, iri = dcOrganization.getIri() + "/" + form.display_id)

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

            Pair(type, dcFormResource)
        }

    }

    private fun convertToDCResourceEM(dcResource: DCResource, form: FormModel): DCResource {

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
            dcResource.setStringValue("citizenreqem:fileContentType", courrierFieds["content_type"].toString())
            dcResource.setStringValue("citizenreqem:fileName", courrierFieds["filename"].toString())
            dcResource.setStringValue("citizenreqem:fileContent", courrierFieds["content"].toString())
        }

        return dcResource
    }

    private fun convertToDCResourceSVE(dcResource: DCResource, form: FormModel): DCResource {

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

    fun getOrCreateUser(form: FormModel): Mono<String> {

        // for now, let's say agent_sictiam is the universal fallback user
        val nameId = if (form.user == null) "5c977a7f1d444fa1ab0f777325fdda93" else form.user.nameID[0]

        return datacoreService.getResourceFromIRI(datacoreProject, datacoreModelUser, nameId, null)
            .map { it.getUri() }
            .switchIfEmpty(Mono.just(1).flatMap { createUser(form.user!!) })
    }

    private fun createUser(user: User): Mono<String> {
        val dcUserResource = DCResource(datacoreProperties.baseResourceUri(),
            type = datacoreModelUser, iri = user.nameID[0])
        dcUserResource.setStringValue("citizenrequser:email", user.email)
        dcUserResource.setStringValue("citizenrequser:nameID", user.nameID[0])
        dcUserResource.setStringValue("citizenrequser:userId", user.id.toString())
        dcUserResource.setStringValue("citizenrequser:name", user.name)
        return datacoreService.saveResource(datacoreProject, datacoreModelUser, dcUserResource, null)
            .map { it.getUri() }
    }

    data class PublikStatusResponse(val url: String?, val err: Int?)

    fun changeStatus(siret: String, dcResource: DCResource) {

        if (dcResource.getStringValue("citizenreq:workflowStatus") != "Terminé") {
            LOGGER.debug("Ignoring unhandled workflow status ${dcResource.getStringValue("citizenreq:workflowStatus")}")
            return
        }

        val businessAppConfigurationMono: Mono<BusinessAppConfiguration> =
            businessAppConfigurationRepository.findByOrganizationSiretAndApplicationName(siret, name)
        val businessMappingMono: Mono<BusinessMapping> =
            businessMappingRepository.findByDcIdAndApplicationName(dcResource.getUri(), name)

        businessAppConfigurationMono
            .zipWith(businessMappingMono)
            .subscribe { tuple2 ->
                val businessAppConfiguration = tuple2.t1
                val businessMapping = tuple2.t2

                val signedQuery =
                    signQuery("email=${businessAppConfiguration.login}&", businessAppConfiguration.secretOrToken!!)
                val uri = "${businessMapping.businessId}jump/trigger/close?$signedQuery"

                val restTemplate = RestTemplate()
                val headers = LinkedMultiValueMap<String, String>()
                headers.set("Accept", "application/json")
                val request = RequestEntity<Any>(null, headers, HttpMethod.POST, URI(uri))

                try {
                    val response = restTemplate.exchange(request, PublikStatusResponse::class.java)
                    LOGGER.debug("Got Publik response for status change $response")
                } catch (e: HttpClientErrorException) {
                    // TODO : what can we do here ?
                    LOGGER.warn("Request status change failed in Publik ${e.responseBodyAsString}")
                }
            }
    }

    private fun signQuery(query: String, secret: String): String {

        val tz = TimeZone.getTimeZone("UTC")
        val df = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").apply { timeZone = tz }
        val thisMoment = df.format(Date())

        val nonce = RandomStringUtils.random(64, true, true)

        val fullQuery = query + "algo=" + this.algo +
                "&timestamp=" + thisMoment +
                "&nonce=" + nonce +
                "&orig=" + this.orig
        val signature = fullQuery.hmac("HmacSHA256", secret)
        return "$fullQuery&signature=$signature"
    }

    @Throws(MalformedURLException::class)
    private fun formatUrl(url_base: String): String {

        LOGGER.debug("Formatting URL $url_base")
        val parsedUrl = URL(url_base)
        return "https://" + parsedUrl.host + "/api/forms" + parsedUrl.path
    }

    private fun isEMrequest(form: FormModel): Boolean {
        LOGGER.debug("Guessing type from ${form.url}")
        return form.url.contains(this.formTypeEM!!)
    }

    private fun isSVErequest(form: FormModel): Boolean {
        return form.id.contains(this.formTypeSVE!!)
    }

    companion object {

        private val LOGGER = LoggerFactory.getLogger(PublikService::class.java)
        const val name: String = "publik"
    }

}
