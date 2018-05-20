package org.ozwillo.dcimporter.service

import com.google.common.io.BaseEncoding
import org.ozwillo.dcimporter.config.FullLoggingInterceptor
import org.ozwillo.dcimporter.model.datacore.*
import org.ozwillo.dcimporter.model.kernel.TokenResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.*
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.client.RestTemplate
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import org.springframework.web.util.UriComponentsBuilder
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.net.URI
import java.net.URISyntaxException
import java.nio.charset.StandardCharsets
import java.util.*

@Service
class DatacoreService {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(DatacoreService::class.java)
    }

    @Value("\${datacore.url: http://localhost:8080}")
    private val datacoreUrl: String = "datacoreUrl"

    @Value("\${publik.datacore.project}")
    private val datacoreProject: String = "datacoreProject"

    @Value("\${publik.datacore.modelORG}")
    private val datacoreModelORG: String = "datacoreModelOrg"

    @Value("\${kernel.auth.token_endpoint: http://localhost:8080}")
    private val tokenEndpoint: String = "http://localhost:8080/a/token"

    @Value("\${kernel.client_id}")
    private val clientId: String = "client_id"

    @Value("\${kernel.client_secret}")
    private val clientSecret: String = "client_secret"

    @Value("\${datacore.systemAdminUser.refreshToken}")
    private val refreshToken: String = "refresh_token"

    fun saveResource(project: String, type: String, resource: DCResourceLight): Mono<DCResultSingle> {

        val uri = UriComponentsBuilder.fromUriString(datacoreUrl)
                .path("/dc/type/{type}")
                .build()
                .expand(type)
                .encode() // ex. orgprfr:OrgPriv%C3%A9e_0 (WITH unencoded ':' and encoded accented chars etc.)
                .toUriString()
        LOGGER.debug("Saving resource at URI $uri")

        val accessToken = getSyncAccessToken()
        val restTemplate = RestTemplate()
        restTemplate.interceptors.add(FullLoggingInterceptor())
        val headers = LinkedMultiValueMap<String, String>()
        headers.set("X-Datacore-Project", project)
        headers.set("Authorization", "Bearer $accessToken")
        val request = RequestEntity<Any>(resource, headers, HttpMethod.POST, URI(uri))

        try {
            val response = restTemplate.exchange(request, DCResourceLight::class.java)
            val result: DCResourceLight = response.body!!
            return Mono.just(DCResultSingle(HttpStatus.OK, result))
        } catch (e: HttpClientErrorException) {
            // TODO : temp hack while not handling existing resource yet
            LOGGER.error("DC returned an error", e)
            return Mono.just(DCResultSingle(HttpStatus.OK, resource))
        }

        // Unused for now, retry and make it clean later
//        return try {
//            val client: WebClient = WebClient.create(uri)
//            getAccessToken().flatMap { accessToken ->
//                client.post()
//                        .header("X-Datacore-Project", project)
//                        .header("Authorization", "Bearer $accessToken")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .syncBody(json)
//                        .exchange()
////                        .onStatus(HttpStatus::is4xxClientError, {
////                            response -> Mono.just(RuntimeException("Received ${response.statusCode()} from DC"))
////                        })
//                        .flatMap { clientResponse -> clientResponse.bodyToMono(DCResource::class.java) }
//                        //.bodyToMono(DCResource::class.java)
//                        .map { it -> DCResultSingle(HttpStatus.OK, it) }
//            }
//        } catch (e: HttpClientErrorException) {
//            this.getDCResultFromHttpErrorException(e)
//        }
    }

    fun getResourceFromURI(project: String, type: String, iri: String): DCBusinessResourceLight {
        val resourceUri = dcResourceUri(type, iri)

        val uri = UriComponentsBuilder.fromUriString(resourceUri.toString())
                .build().encode().toUriString()

        LOGGER.debug("Fetching resource from URI $uri")

        val accessToken = getAccessToken().block()!!
        val restTemplate = RestTemplate()
        val headers = LinkedMultiValueMap<String, String>()
        headers.set("X-Datacore-Project", project)
        headers.set("Authorization", "Bearer $accessToken")
        val request = RequestEntity<Any>(headers, HttpMethod.GET, URI(uri))

        try {
            val response = restTemplate.exchange(request, DCBusinessResourceLight::class.java)
            LOGGER.debug("Got response : ${response.body}")
            val result: DCBusinessResourceLight = response.body!!
            return result
        } catch (e: HttpClientErrorException) {
            LOGGER.error("Error while retrieving resource", e)
            return DCBusinessResourceLight(uri = resourceUri.toString(),
                    values = mapOf(Pair("citizenrequser:name", "John Doe"),
                                    Pair("citizenrequser:email", "unknown@doe.fr")))
        }

//        return try {
//            val client: WebClient = WebClient.create(uri)
//            getAccessToken().flatMap { accessToken ->
//                client.get()
//                        .header("X-Datacore-Project", project)
//                        .header("Authorization", "Bearer $accessToken")
//                        .accept(MediaType.APPLICATION_JSON)
//                        .retrieve()
//                        .bodyToMono<DCBusinessResourceLight>()
//            }
//        } catch (e: HttpClientErrorException) {
//            Mono.empty()
//        }
    }

    fun getDCOrganization(orgLegalName: String): Mono<DCResourceLight> {
        val queryParametersOrg = DCQueryParameters("org:legalName", DCOperator.EQ, DCOrdering.DESCENDING, orgLegalName)
        return findResource(datacoreProject, datacoreModelORG, queryParametersOrg).map { it[0] }
    }

    fun findResource(project: String, model: String, queryParameters: DCQueryParameters): Mono<List<DCResourceLight>> {

        val uriComponentsBuilder = UriComponentsBuilder.fromUriString(datacoreUrl)
                .path("/dc/type/{type}")
                .queryParam("start", 0)
                .queryParam("limit", 1)

        for (param in queryParameters) {
            uriComponentsBuilder.queryParam(param.subject, param.operator.value + param.getObject())
            // ex. {start=[0], limit=[11], geo:name.v=[$regex^Zamor], geo:country=[http://data.ozwillo.com/dc/type/geocoes:Pa%C3%ADs_0/ES]}
        }

        val uriComponents = uriComponentsBuilder
                .build()
                .expand(model)
                .encode()
        // path ex. orgprfr:OrgPriv%C3%A9e_0 (WITH unencoded ':' and encoded accented chars etc.)
        // and query ex. geo:name.v=$regex%5EZamor&geo:country=http://data.ozwillo.com/dc/type/geocoes:Pa%25C3%25ADs_0/ES
        // NB. This will also encode all parameters including the regex ^ and other matches like "geo:country=http..." which is wrong

        val requestUri = uriComponents.toUriString()
        // and NOT uriComponents.toString() else variable expansion encodes it once too many
        // (because new UriTemplate(uriString) assumes uriString is not yet encoded -_-)
        // ex. https://plnm-dev-dc/dc/type/geoci:City_0?start=0&limit=11&geo:name.v=$regex%5EZamor&geo:country=http://data.ozwillo.com/dc/type/geocoes:Pa%25C3%25ADs_0/ES

        LOGGER.debug("Fetching limited resources from URI $requestUri")

        //val client: WebClient = WebClient.create(requestUri)

        val accessToken = getAccessToken().block()!!
        val restTemplate = RestTemplate()
        val headers = LinkedMultiValueMap<String, String>()
        headers.set("X-Datacore-Project", project)
        headers.set("Authorization", "Bearer $accessToken")
        val request = RequestEntity<Any>(headers, HttpMethod.GET, URI(requestUri))
        val respType = object: ParameterizedTypeReference<List<DCResourceLight>>(){}

        val response = restTemplate.exchange(request, respType)
        val results: List<DCResourceLight> = response.body!!
        return Mono.just(results)

//        return getAccessToken().flatMap { accessToken ->
//            client.get()
//                    .header("X-Datacore-Project", project)
//                    .header("Authorization", "Bearer $accessToken")
//                    .accept(MediaType.APPLICATION_JSON)
//                    .retrieve()
//                    .bodyToMono<List<DCResourceLight>>()
//        }
    }

    fun findResources(project: String, model: String, queryParameters: DCQueryParameters, start: Int, maxResult: Int): Flux<DCResource> {

        val uriComponentsBuilder = UriComponentsBuilder.fromUriString(datacoreUrl)
                .path("/dc/type/{type}")
                .queryParam("start", start)
                .queryParam("limit", maxResult)

        for (param in queryParameters) {
            uriComponentsBuilder.queryParam(param.subject, param.operator.value + param.getObject())
            // ex. {start=[0], limit=[11], geo:name.v=[$regex^Zamor], geo:country=[http://data.ozwillo.com/dc/type/geocoes:Pa%C3%ADs_0/ES]}
        }

        val uriComponents = uriComponentsBuilder
                .build()
                .expand(model)
                .encode()
        // path ex. orgprfr:OrgPriv%C3%A9e_0 (WITH unencoded ':' and encoded accented chars etc.)
        // and query ex. geo:name.v=$regex%5EZamor&geo:country=http://data.ozwillo.com/dc/type/geocoes:Pa%25C3%25ADs_0/ES
        // NB. This will also encode all parameters including the regex ^ and other matches like "geo:country=http..." which is wrong

        val requestUri = uriComponents.toUriString()
        // and NOT uriComponents.toString() else variable expansion encodes it once too many
        // (because new UriTemplate(uriString) assumes uriString is not yet encoded -_-)
        // ex. https://plnm-dev-dc/dc/type/geoci:City_0?start=0&limit=11&geo:name.v=$regex%5EZamor&geo:country=http://data.ozwillo.com/dc/type/geocoes:Pa%25C3%25ADs_0/ES

        if (LOGGER.isDebugEnabled) {
            LOGGER.debug("Fetching limited Resources: URI String is " + requestUri)
        }

        return try {
            val client: WebClient = WebClient.create(requestUri)
            getAccessToken().flatMapMany { accessToken ->
                client.get()
                        .header("X-Datacore-Project", project)
                        .header("Authorization", "Bearer $accessToken")
                        .accept(MediaType.APPLICATION_JSON)
                        .retrieve()
                        .bodyToFlux(DCResource::class.java)
            }
        } catch (e: HttpClientErrorException) {
            Flux.empty() // this.getDCResultFromHttpErrorException(e)
        }
    }

    private fun getDCResultFromHttpErrorException(e: HttpStatusCodeException): Mono<DCResult> {
        LOGGER.error("Error caught while querying data core", e)
        LOGGER.debug("Response body: {}", e.responseBodyAsString)
        return Mono.just(DCResultError(HttpStatus.valueOf(e.statusCode.value()), e.responseBodyAsString))
    }

    private fun getAccessToken(): Mono<String> {
        val client: WebClient = WebClient.create(tokenEndpoint)
        val authorizationHeaderValue: String = "Basic " + BaseEncoding.base64().encode(
                String.format(Locale.ROOT, "%s:%s", clientId, clientSecret).toByteArray(StandardCharsets.UTF_8))
        return client.post()
                .header("Authorization", authorizationHeaderValue)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("grant_type", "refresh_token").with("refresh_token", refreshToken))
                .retrieve()
                .bodyToMono(TokenResponse::class.java)
                .map { it -> it.accessToken }
    }

    private fun getSyncAccessToken(): String {

        val restTemplate = RestTemplate()

        val authorizationHeaderValue: String = "Basic " + BaseEncoding.base64().encode(
                String.format(Locale.ROOT, "%s:%s", clientId, clientSecret).toByteArray(StandardCharsets.UTF_8))
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_FORM_URLENCODED
        headers.set("Authorization", authorizationHeaderValue)
        val map = LinkedMultiValueMap<String, String>()
        map.add("grant_type", "refresh_token")
        map.add("refresh_token", refreshToken)

        val request = HttpEntity<MultiValueMap<String, String>>(map, headers)
        val response = restTemplate.postForEntity(tokenEndpoint, request, TokenResponse::class.java)

        return response.body!!.accessToken!! //Mono.just(DCResultSingle(HttpStatus.OK, result))
    }

    private fun dcResourceUri(resourceType: String, iri: String): URI {
        return builderToUri(dcResourceUriBuilder(resourceType, iri, "/dc/type/"))
    }

    /**
     * avoids URISyntaxException
     */
    private fun builderToUri(sb: StringBuilder): URI {
        try {
            return URI(sb.toString())
        } catch (e: URISyntaxException) {
            throw IllegalArgumentException(e)
        }
    }

    private fun dcResourceUriBuilder(resourceType: String, resourceIri: String, apiUriPart: String): StringBuilder {
        return dcResourceTypeUriBuilder(resourceType, apiUriPart)
            .append('/')
            .append(resourceIri) // already encoded
    }

    private fun dcResourceTypeUriBuilder(resourceType: String, apiUriPart: String): StringBuilder {
        return StringBuilder(datacoreUrl)
                .append(apiUriPart)
                .append(DCResource.encodeUriPathSegment(resourceType))
    }
}

typealias DCModelType = String
