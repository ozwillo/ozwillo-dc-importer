package org.ozwillo.dcimporter.service

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import java.net.URI
import org.ozwillo.dcimporter.config.DatacoreProperties
import org.ozwillo.dcimporter.config.FullLoggingInterceptor
import org.ozwillo.dcimporter.config.InseeSireneProperties
import org.ozwillo.dcimporter.model.datacore.DCResource
import org.ozwillo.dcimporter.model.kernel.TokenResponse
import org.ozwillo.dcimporter.model.sirene.Organization
import org.slf4j.LoggerFactory
import org.springframework.http.*
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder

@Service
class InseeSireneService(
    private val inseeSireneProperties: InseeSireneProperties,
    private val datacoreProperties: DatacoreProperties
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    fun getSireneToken(): String {
        val restTemplate = RestTemplate()

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_FORM_URLENCODED
        headers.set("Authorization", "Basic ${inseeSireneProperties.secretClient}")
        val map = LinkedMultiValueMap<String, String>()
        map.add("grant_type", "client_credentials")

        val request = HttpEntity<MultiValueMap<String, String>>(map, headers)

        val response: ResponseEntity<TokenResponse> =
            restTemplate.postForEntity("${inseeSireneProperties.baseUri}${inseeSireneProperties.tokenPath}", request, TokenResponse::class.java)
        return response.body!!.accessToken!!
    }

    fun getOrgFromSireneAPI(siret: String): DCResource {
        val sireneToken = "Bearer ${getSireneToken()}"
        val encodedUri =
            UriComponentsBuilder.fromUriString("${inseeSireneProperties.baseUri}${inseeSireneProperties.siretPath}/$siret$${inseeSireneProperties.siretParameters}").build().encode()
                .toUriString()
        val restTemplate = RestTemplate()

        val headers = LinkedMultiValueMap<String, String>()
        headers.set("Authorization", sireneToken)

        val request = RequestEntity<Any>(headers, HttpMethod.GET, URI(encodedUri))
        restTemplate.interceptors.add(FullLoggingInterceptor())
        val response: ResponseEntity<String> = restTemplate.exchange(request, String::class.java)
        logger.debug(response.body!!)

        val mapper = ObjectMapper()
        val responseObject: JsonNode = mapper.readTree(response.body)

        val cp =
            responseObject.get("etablissement").get("adresseEtablissement").get("codePostalEtablissement").textValue()
        val numero =
            responseObject.get("etablissement").get("adresseEtablissement").get("numeroVoieEtablissement").textValue()
        val typeVoie =
            responseObject.get("etablissement").get("adresseEtablissement").get("typeVoieEtablissement").textValue()
        val libelleVoie =
            responseObject.get("etablissement").get("adresseEtablissement").get("libelleVoieEtablissement").textValue()
        val denominationUniteLegale =
            responseObject.get("etablissement").get("uniteLegale").get("denominationUniteLegale")
        val nomUniteLegale = responseObject.get("etablissement").get("uniteLegale").get("nomUniteLegale")
        val finalDenomination =
            if (!denominationUniteLegale.isNull) denominationUniteLegale.textValue() else nomUniteLegale.textValue()
        val siretEtablissement = responseObject.get("etablissement").get("siret").textValue()

        val organization = Organization(
            cp = cp,
            voie = "$numero $typeVoie $libelleVoie",
            pays = "${datacoreProperties.baseResourceUri()}/geocofr:Pays_0/FR",
            denominationUniteLegale = finalDenomination,
            siret = siretEtablissement
        )

        return organization.toDcObject(datacoreProperties.baseResourceUri(), siret)
    }
}
