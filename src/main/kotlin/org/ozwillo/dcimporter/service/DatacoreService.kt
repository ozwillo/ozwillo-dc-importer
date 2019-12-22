package org.ozwillo.dcimporter.service

import java.net.URI
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.util.*
import org.ozwillo.dcimporter.config.DatacoreProperties
import org.ozwillo.dcimporter.config.KernelProperties
import org.ozwillo.dcimporter.model.datacore.*
import org.ozwillo.dcimporter.model.kernel.TokenResponse
import org.ozwillo.dcimporter.service.rabbitMQ.Sender
import org.ozwillo.dcimporter.util.BindingKeyAction
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.*
import org.springframework.stereotype.Service
import org.springframework.util.Base64Utils
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.util.UriComponentsBuilder
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
class DatacoreService(private val kernelProperties: KernelProperties, private val datacoreProperties: DatacoreProperties) {

    @Autowired
    private lateinit var sender: Sender

    companion object {
        private val LOGGER = LoggerFactory.getLogger(DatacoreService::class.java)
    }

    fun saveResource(project: String, type: String, resource: DCResource, bearer: String?): Mono<DCResource> {

        val uri = UriComponentsBuilder.fromUriString(datacoreProperties.url)
            .path("/dc/type/{type}")
            .build()
            .expand(type)
            .encode() // ex. orgprfr:OrgPriv%C3%A9e_0 (WITH unencoded ':' and encoded accented chars etc.)
            .toUriString()
        LOGGER.debug("Saving resource $resource at URI $uri")

        val accessToken = bearer ?: getSyncAccessToken()

        return WebClient.create().post()
            .uri(uri)
            .header("X-Datacore-Project", project)
            .header("Authorization", "Bearer $accessToken")
            .bodyValue(resource)
            .retrieve()
            .onStatus(
                HttpStatus::is4xxClientError
            ) { clientResponse ->
                clientResponse.bodyToMono(String::class.java)
                    .flatMap {
                        LOGGER.error("Got error while saving resource : $it")
                        Mono.just(HttpClientErrorException(HttpStatus.BAD_REQUEST))
                    }
            }
            .bodyToMono(DCResource::class.java)
            .doOnSuccess {
                sender.send(resource, project, type, BindingKeyAction.CREATE)
            }
    }

    fun updateResource(project: String, type: String, resource: DCResource, bearer: String?): Mono<HttpStatus> {

        val uri = UriComponentsBuilder.fromUriString(datacoreProperties.url)
            .path("/dc/type/{type}")
            .build()
            .expand(type)
            .encode() // ex. orgprfr:OrgPriv%C3%A9e_0 (WITH unencoded ':' and encoded accented chars etc.)
            .toUriString()

        LOGGER.debug("Updating resource at URI $uri")

        return getResourceFromIRI(project, type, resource.getIri(), bearer)
            .map {
                resource.setStringValue(
                    "o:version",
                    it.getValues().getOrElse("o:version") { "0" }.toString())
                resource
            }.flatMap {
                val accessToken = bearer ?: getSyncAccessToken()
                WebClient.create().put()
                    .uri(uri)
                    .header("X-Datacore-Project", project)
                    .header("Authorization", "Bearer $accessToken")
                    .bodyValue(it)
                    .retrieve()
                    .bodyToMono(DCResource::class.java)
            }.map {
                sender.send(resource, project, type, BindingKeyAction.CREATE)
                HttpStatus.OK
            }
    }

    fun exists(project: String, type: String, iri: String, bearer: String?): Mono<Boolean> {
        val resourceUri = checkEncoding(
            dcResourceUri(
                type,
                iri
            )
        ) // If dcResourceIri already encoded return the decoded version to avoid % encoding to %25 ("some iri" -> "some%20iri" -> "some%2520iri")
        val encodedUri = UriComponentsBuilder.fromUriString(resourceUri).build().encode().toUriString()

        LOGGER.debug("Checking existence of resource $encodedUri")

        val accessToken = bearer ?: getSyncAccessToken()
        return WebClient.create().get()
            .uri(encodedUri)
            .header("X-Datacore-Project", project)
            .header("Authorization", "Bearer $accessToken")
            .exchange()
            .flatMap { Mono.just(true) }
            .onErrorResume { Mono.just(false) }
    }

    fun getResourceFromIRI(project: String, type: String, iri: String, bearer: String?): Mono<DCResource> {
        val resourceUri = checkEncoding(
            dcResourceUri(
                type,
                iri
            )
        ) // If dcResourceIri already encoded return the decoded version to avoid % encoding to %25 ("some iri" -> "some%20iri" -> "some%2520iri")
        val encodedUri = UriComponentsBuilder.fromUriString(resourceUri).build().encode().toUriString()

        LOGGER.debug("Fetching resource from URI $encodedUri")

        val accessToken = bearer ?: getSyncAccessToken()
        return WebClient.create().get()
            .uri(encodedUri)
            .header("X-Datacore-Project", project)
            .header("Authorization", "Bearer $accessToken")
            .retrieve()
            .bodyToMono(DCResource::class.java)
            .onErrorResume { Mono.empty() }
    }

    private fun checkEncoding(iri: String): String {
        val decodedIri = URLDecoder.decode(iri, "UTF-8")
        return if (decodedIri.length < iri.length) {
            decodedIri
        } else {
            iri
        }
    }

    fun findResource(project: String, model: String, queryParameters: DCQueryParameters): Mono<List<DCResource>> {

        val uriComponentsBuilder = UriComponentsBuilder.fromUriString(datacoreProperties.url)
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

        // val client: WebClient = WebClient.create(requestUri)

        val accessToken = getSyncAccessToken()
        val restTemplate = RestTemplate()
        val headers = LinkedMultiValueMap<String, String>()
        headers.set("X-Datacore-Project", project)
        headers.set("Authorization", "Bearer $accessToken")
        val request = RequestEntity<Any>(headers, HttpMethod.GET, URI(requestUri))
        val respType = object : ParameterizedTypeReference<List<DCResource>>() {}

        val response = restTemplate.exchange(request, respType)
        val results: List<DCResource> = response.body!!
        return Mono.just(results)
    }

    fun findModels(limit: Int, name: String): Flux<DCModel> {

        val uriComponentsBuilder = UriComponentsBuilder.fromUriString(datacoreProperties.url)
            .path("/dc/type/dcmo:model_0")
            .queryParam("limit", limit)
            .queryParam("dcmo:name", "${DCOperator.REGEX.value}$name")

        val uriComponents = uriComponentsBuilder
            .build()
            .encode()

        val requestUri = uriComponents.toUriString()

        return try {
            val client: WebClient = WebClient.create(requestUri)
            getAccessToken().flatMapMany { accessToken ->
                client.get()
                    .header("Authorization", "Bearer $accessToken")
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToFlux(DCModel::class.java)
            }
        } catch (e: HttpClientErrorException) {
            Flux.empty() // this.getDCResultFromHttpErrorException(e)
        }
    }

    fun findModel(type: String): Mono<DCModel> {

        val uri = UriComponentsBuilder.fromUriString(datacoreProperties.url)
            .path("/dc/type/dcmo:model_0/{type}")
            .build()
            .expand(type)
            .encode()
            .toUriString()

        LOGGER.debug("Fetching model, URI String is {}", uri)

        return try {
            val client: WebClient = WebClient.create(uri)

            return client.get()
                .header("Authorization", "Bearer ${getSyncAccessToken()}")
                .header("X-Datacore-Project", "oasis.main")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(DCModel::class.java)
        } catch (e: HttpClientErrorException) {
            Mono.empty()
        }
    }

    fun findResources(
        project: String,
        model: String,
        queryParameters: DCQueryParameters,
        start: Int,
        maxResult: Int
    ): Flux<DCResource> {

        val uriComponentsBuilder = UriComponentsBuilder.fromUriString(datacoreProperties.url)
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

        // Okay so to avoid encoded parameters and as long I can't directly modify uriComponents.query alone : I decode uriComponents.query and replace everything following the "?" in the final String query by it
        val decodedQuery = URLDecoder.decode(uriComponents.query, "UTF-8")

        val requestUri = uriComponents.toUriString().substringBefore("?") + "?" + decodedQuery
        // and NOT uriComponents.toString() else variable expansion encodes it once too many
        // (because new UriTemplate(uriString) assumes uriString is not yet encoded -_-)
        // ex. https://plnm-dev-dc/dc/type/geoci:City_0?start=0&limit=11&geo:name.v=$regex%5EZamor&geo:country=http://data.ozwillo.com/dc/type/geocoes:Pa%25C3%25ADs_0/ES

        if (LOGGER.isDebugEnabled) {
            LOGGER.debug("Fetching limited Resources: URI String is $requestUri")
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

    private fun getAccessToken(): Mono<String> {
        val client: WebClient = WebClient.create(kernelProperties.tokenEndpoint)
        val authorizationHeaderValue: String = "Basic " + Base64Utils.encodeToString(
            String.format(Locale.ROOT, "%s:%s", kernelProperties.clientId, kernelProperties.clientSecret).toByteArray(
                StandardCharsets.UTF_8
            )
        )
        return client.post()
            .header("Authorization", authorizationHeaderValue)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(BodyInserters.fromFormData("grant_type", "refresh_token")
                .with("refresh_token", datacoreProperties.systemAdminUser.refreshToken))
            .retrieve()
            .bodyToMono(TokenResponse::class.java)
            .map { it.accessToken }
    }

    private fun getSyncAccessToken(): String {

        val restTemplate = RestTemplate()

        val authorizationHeaderValue: String = "Basic " + Base64Utils.encodeToString(
            String.format(Locale.ROOT, "%s:%s", kernelProperties.clientId, kernelProperties.clientSecret).toByteArray(
                StandardCharsets.UTF_8
            )
        )
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_FORM_URLENCODED
        headers.set("Authorization", authorizationHeaderValue)
        val map = LinkedMultiValueMap<String, String>()
        map.add("grant_type", "refresh_token")
        map.add("refresh_token", datacoreProperties.systemAdminUser.refreshToken)

        val request = HttpEntity<MultiValueMap<String, String>>(map, headers)
        val response = restTemplate.postForEntity(kernelProperties.tokenEndpoint, request, TokenResponse::class.java)

        return response.body!!.accessToken!! // Mono.just(DCResultSingle(HttpStatus.OK, result))
    }

    private fun dcResourceUri(type: DCModelType, iri: String): String {
        return StringBuilder(datacoreProperties.url)
            .append(datacoreProperties.typePrefix)
            .append('/')
            .append(type.encodeUriPathSegment())
            .append('/')
            .append(iri) // already encoded
            .toString()
    }
}
