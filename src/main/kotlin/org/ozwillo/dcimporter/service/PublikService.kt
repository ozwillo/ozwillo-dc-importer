package org.ozwillo.dcimporter.service

import org.apache.commons.codec.binary.Base64
import org.oasis_eu.spring.datacore.DatacoreClient
import org.oasis_eu.spring.datacore.model.*
import org.ozwillo.dcimporter.config.Prop
import org.ozwillo.dcimporter.model.publik.FormModel
import org.ozwillo.dcimporter.model.publik.ListFormsModel
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import reactor.core.publisher.Mono
import java.io.UnsupportedEncodingException
import java.text.SimpleDateFormat
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import javax.xml.bind.DatatypeConverter

import java.net.MalformedURLException
import java.net.URI
import java.net.URISyntaxException
import java.net.URL
import java.net.URLEncoder

@Service
class PublikService(private val datacoreClient: DatacoreClient,
                    private val systemUserService: SystemUserService,
                    private val props: Prop) {

    private val restTemplate = RestTemplate()

    @Value("\${publik.formTypeEM}")
    private val formTypeEM: String? = null
    @Value("\${publik.formTypeSVE}")
    private val formTypeSVE: String? = null
    @Value("\${publik.algo}")
    private val algo: String? = null
    @Value("\${publik.orig}")
    private val orig: String? = null
    @Value("\${publik.secret}")
    private val secret: String? = null
    @Value("\${publik.datacore.project}")
    private val datacoreProject: String? = null
    @Value("\${publik.datacore.modelEM}")
    private val datacoreModelEM: String? = null
    @Value("\${publik.datacore.modelSVE}")
    private val datacoreModelSVE: String? = null
    @Value("\${publik.datacore.modelORG}")
    private val datacoreModelORG: String? = null
    @Value("\${publik.datacore.modelUser}")
    private val datacoreModelUser: String? = null
    @Value("\${datacore.baseUri}")
    private val datacoreBaseUri: String? = null

    @Throws(URISyntaxException::class)
    private fun getForm(url: String): FormModel? {

        val finalUrl = sign_url(url)
        LOGGER.error("URL get Form {}", finalUrl)

        return restTemplate.getForObject(finalUrl!!, FormModel::class.java)
    }

    fun syncPublikForms(baseUrl: String, dcOrganization: DCResource, formType: String) {

        val initUrl = "$baseUrl/api/forms/$formType/list?anonymise"

        try {
            val url = sign_url(initUrl)
            LOGGER.debug("Calling Publik at URL {}", url)
            val forms = restTemplate.getForObject(url!!, Array<ListFormsModel>::class.java)

            forms!!
                    .filter { it.url.isEmpty() }
                    .map { getForm(formatUrl(it.url)) }
                    .map { convertToDCResource(dcOrganization, it!!) }
                    .forEach { systemUserService.runAs( { datacoreClient.saveResource(datacoreProject, it) }) }
        } catch (e: URISyntaxException) {
            LOGGER.error("Exception Uri Syntax : " + e)
        } catch (e: MalformedURLException) {
            LOGGER.error("MalformedURLException : " + e)
        }

    }

    fun saveResourceToDC(form: FormModel): Mono<DCResult> {

        val orgLegalName: String? =
                props.instance.first { !it["baseUrl"]!!.isEmpty() && it["baseUrl"]!!.contains(form.url)}["organization"]

        val dcOrganization = getDCOrganization(orgLegalName)
        if (!dcOrganization.isPresent) {
            LOGGER.error("Unable to get organization {}", orgLegalName)
            return Mono.empty()
        }

        val result = systemUserService.runAs( { datacoreClient.saveResource(datacoreProject, convertToDCResource(dcOrganization.get(), form)) } )
        return Mono.just(result)
    }

    private fun convertToDCResource(dcOrganization: DCResource, form: FormModel): DCResource {

        val dcResource = DCResource()

        dcResource.baseUri = datacoreBaseUri

        dcResource.iri = dcOrganization.iri + "/" + form.display_id

        dcResource.set("citizenreq:displayId", form.display_id)
        dcResource.set("citizenreq:lastUpdateTime", form.last_update_time)
        dcResource.set("citizenreq:displayName", form.display_name)
        dcResource.set("citizenreq:submissionChannel", form.submission.channel)
        dcResource.set("citizenreq:submissionBackoffice", form.submission.backoffice.toString())
        dcResource.set("citizenreq:url", form.url)
        dcResource.set("citizenreq:receiptTime", form.receipt_time)

        dcResource.set("citizenreq:criticalityLevel", form.criticality_level.toString())
        dcResource.set("citizenreq:id", form.id)
        dcResource.set("citizenreq:organization", dcOrganization.uri)

        dcResource.set("citizenreq:user", createUserDCResource(form))

        if (isEMrequest(form)) {
            convertToDCResourceEM(dcResource, form)
        } else if (isSVErequest(form)) {
            convertToDCResourceSVE(dcResource, form)
        }
        LOGGER.debug("DCResouce --> :" + dcResource.toString())
        return dcResource
    }

    private fun convertToDCResourceEM(dcResource: DCResource, form: FormModel): DCResource {

        dcResource.type = datacoreModelEM

        dcResource.set("citizenreqem:familyName", form.fields["nom_famille"].toString())
        dcResource.set("citizenreqem:firstName", form.fields["prenom"].toString())
        dcResource.set("citizenreqem:phone", form.fields["telephone"].toString())
        if (form.fields["detail"] != null)
            dcResource.set("citizenreqem:detail", form.fields["detail"].toString())
        dcResource.set("citizenreqem:objectSummary", form.fields["objet_rendez_vous_raw"].toString())
        dcResource.set("citizenreqem:objectDetail", form.fields["objet_rendez_vous"].toString())
        dcResource.set("citizenreqem:desiredDate", form.fields["date_souhaitee"].toString())
        dcResource.set("citizenreqem:email", form.fields["courriel"].toString())

        return dcResource
    }

    private fun convertToDCResourceSVE(dcResource: DCResource, form: FormModel): DCResource {

        dcResource.type = datacoreModelSVE

        dcResource.set("citizenreqsve:title", form.fields["civilite"].toString())
        dcResource.set("citizenreqsve:familyName", form.fields["nom"].toString())
        dcResource.set("citizenreqsve:firstName", form.fields["prenoms"].toString())
        dcResource.set("citizenreqsve:email", form.fields["email"].toString())
        dcResource.set("citizenreqsve:streetAddress", form.fields["voie"].toString())
        dcResource.set("citizenreqsve:zipCode", form.fields["code_postal"].toString())
        dcResource.set("citizenreqsve:city", form.fields["commune"].toString())
        dcResource.set("citizenreqsve:entityType", form.fields["entite_raw"].toString())
        dcResource.set("citizenreqsve:object", form.fields["objet"].toString())
        dcResource.set("citizenreqsve:message", form.fields["message"].toString())

        val doc = form.fields["doc"] as HashMap<String, String>
        dcResource.set("citizenreqsve:docContent", doc["content"])
        dcResource.set("citizenreqsve:docFieldId", doc["field_id"])
        dcResource.set("citizenreqsve:docContentType", doc["content_type"])
        dcResource.set("citizenreqsve:docFileName", doc["filename"])

        if (form.fields["siret"] != null)
            dcResource.set("citizenreqsve:siret", form.fields["siret"].toString())
        if (form.fields["siret_entreprise"] != null)
            dcResource.set("citizenreqsve:siret", form.fields["siret_entreprise"].toString())
        if (form.fields["rna"] != null)
            dcResource.set("citizenreqsve:rna", form.fields["rna"].toString())
        if (form.fields["nom_entite_entreprise"] != null)
            dcResource.set("citizenreqsve:legalName", form.fields["nom_entite_entreprise"].toString())
        if (form.fields["nom_entite"] != null)
            dcResource.set("citizenreqsve:entityName", form.fields["nom_entite"].toString())
        return dcResource
    }

    private fun createUserDCResource(form: FormModel): String {

        val dcResource = DCResource()

        dcResource.baseUri = datacoreBaseUri
        dcResource.type = datacoreModelUser
        dcResource.iri = form.user.nameID[0]

        systemUserService.runAs( {
            if (datacoreClient.getResourceFromURI(datacoreProject, dcResource.uri).resource == null) {

                dcResource.set("citizenrequser:email", form.user.email)
                dcResource.set("citizenrequser:nameID", form.user.nameID[0])
                dcResource.set("citizenrequser:userId", form.user.id.toString())
                dcResource.set("citizenrequser:name", form.user.name)
                datacoreClient.saveResource(datacoreProject, dcResource)
            } else {
                DCResult(DCResultType.SUCCESS)
            }
        })

        return dcResource.uri
    }

    fun getDCOrganization(orgLegalName: String?): Optional<DCResource> {
        val queryParametersOrg = DCQueryParameters("org:legalName", DCOperator.EQ, orgLegalName)
        val resources = systemUserService.runAs( {
            val results = datacoreClient
                    .findResources(datacoreProject, datacoreModelORG, queryParametersOrg, 0, 1)
            DCResult(DCResultType.SUCCESS, results)
        }).resources

        return if (resources.isEmpty()) Optional.empty() else Optional.of(resources[0])
    }

    /**
     * Calculate a signature with sha256
     *
     * @return
     */
    private fun calculateSignature(message: String, key: String?): String? {

        try {

            val sha256_HMAC = Mac.getInstance("HmacSHA256")
            val secret_key = SecretKeySpec(key!!.toByteArray(), "HmacSHA256")
            sha256_HMAC.init(secret_key)

            var hash = Base64.encodeBase64String(sha256_HMAC.doFinal(message.toByteArray()))
            LOGGER.debug("Signature : " + hash)

            hash = URLEncoder.encode(hash, "UTF-8")
            LOGGER.debug("URL encoded hash : " + hash)

            return hash
        } catch (e: Exception) {
            LOGGER.error("Exception when calculate the signature : " + e)
            return null
        }

    }

    @Throws(URISyntaxException::class)
    private fun sign_url(url: String): URI? {

        val tz = TimeZone.getTimeZone("UTC")
        val df = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
        df.timeZone = tz
        val thisMoment = df.format(Date())

        val random = Random()
        // create byte array
        val nbyte = ByteArray(16)
        // put the next byte in the array
        random.nextBytes(nbyte)
        val nonce = DatatypeConverter.printHexBinary(nbyte)

        try {
            var newQuery = ""
            val parsedUrl = URL(url)

            if (parsedUrl.query != null)
                newQuery = parsedUrl.query + "&"

            newQuery += ("algo=" + this.algo + "&timestamp=" + URLEncoder.encode(thisMoment, "UTF-8") + "&nonce=" + nonce
                    + "&orig=" + URLEncoder.encode(this.orig!!, "UTF-8"))
            val signature = calculateSignature(newQuery, this.secret)
            newQuery += "&signature=" + signature!!

            return URI(
                    parsedUrl.protocol + "://" + parsedUrl.host + parsedUrl.path + "?" + newQuery)
        } catch (e: MalformedURLException) {
            LOGGER.error("MalformedURLException : " + e)
            return null
        } catch (e: UnsupportedEncodingException) {
            LOGGER.error("Unsupported encoding exception !?")
            return null
        }

    }

    @Throws(MalformedURLException::class)
    private fun formatUrl(url_base: String): String {

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
    }

}