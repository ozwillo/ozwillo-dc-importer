package org.ozwillo.dcimporter.service

import java.net.URLDecoder
import org.ozwillo.dcimporter.config.DatacoreProperties
import org.ozwillo.dcimporter.model.datacore.*
import org.ozwillo.dcimporter.service.rabbitMQ.Sender
import org.ozwillo.dcimporter.util.BindingKeyAction
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.util.UriComponentsBuilder
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import reactor.kotlin.core.publisher.toFlux

@Service
class DatacoreService(
    private val datacoreProperties: DatacoreProperties,
    private val inseeSireneService: InseeSireneService
) {

    @Autowired
    private lateinit var sender: Sender

    @Value("\${datacore.model.modelORG}")
    private val modelOrg = ""

    private val logger = LoggerFactory.getLogger(javaClass)

    fun saveResource(project: String, type: String, resource: DCResource, bearer: String): Mono<DCResource> {

        val uri = encodedUrlToType(type)
        logger.debug("Saving resource $resource at URI $uri")

        return WebClient.create().post()
            .uri(uri)
            .header("X-Datacore-Project", project)
            .header("Authorization", "Bearer $bearer")
            .bodyValue(resource)
            .retrieve()
            .onStatus(HttpStatus::is4xxClientError, this::unwrapDatacoreError)
            .bodyToMono(DCResource::class.java)
            .doOnSuccess {
                sender.send(resource, project, type, BindingKeyAction.CREATE)
            }
    }

    var dcResourceListTypeRef: ParameterizedTypeReference<List<DCResource>> =
        object : ParameterizedTypeReference<List<DCResource>>() {}

    fun updateResource(project: String, type: String, resource: DCResource, bearer: String): Mono<DCResource> {

        val uri = encodedUrlToType(type)
        logger.debug("Updating resource at URI $uri")

        val resourceVersion = Mono.justOrEmpty(resource.getVersion())
            .switchIfEmpty {
                getResourceFromIRI(project, type, resource.getIri(), bearer)
                    .map {
                        it.getIntValue("o:version")
                    }
            }

        return resourceVersion
            .map {
                resource.setIntegerValue("o:version", it)
                resource
            }.flatMap {
                WebClient.create().put()
                    .uri(uri)
                    .header("X-Datacore-Project", project)
                    .header("Authorization", "Bearer $bearer")
                    .bodyValue(it)
                    .retrieve()
                    .onStatus(HttpStatus::is4xxClientError, this::unwrapDatacoreError)
                    .bodyToMono(dcResourceListTypeRef)
            }.map {
                it[0]
            }.doOnSuccess {
                sender.send(resource, project, type, BindingKeyAction.UPDATE)
            }
    }

    fun checkAndCreateLinkedResources(project: String, bearer: String, dcResource: DCResource): Mono<List<DCResource>> {
        return dcResource.getValues()
            .filterValues { v -> v.toString().contains("${datacoreProperties.baseResourceUri()}/$modelOrg") }
            .map { it.value as String }
            .toFlux()
            .flatMap { findOrCreateDCOrganization(project, bearer, it) }
            .collectList()
    }

    private fun findOrCreateDCOrganization(project: String, bearer: String, organizationUri: String): Mono<DCResource> {

        val siret = organizationUri.substringAfterLast("/")

        return exists(project, modelOrg, "FR/$siret", bearer)
            .filter { it == false }
            .map { inseeSireneService.getOrgFromSireneAPI(siret) }
            .flatMap { saveResource(project, modelOrg, it, bearer) }
    }

    fun deleteResource(project: String, type: String, iri: String, bearer: String): Mono<Boolean> {
        val resourceUri = checkEncoding(dcResourceUri(type, iri))
        val encodedUri = UriComponentsBuilder.fromUriString(resourceUri).build().encode().toUriString()

        return getResourceFromIRI(project, type, iri, bearer)
            .flatMap {
                WebClient.create().delete()
                    .uri(encodedUri)
                    .header("X-Datacore-Project", project)
                    .header("Authorization", "Bearer $bearer")
                    .header("If-Match", it.getIntValue("o:version").toString())
                    .exchange()
            }
            .map {
                it.statusCode() == HttpStatus.NO_CONTENT
            }
            .doOnSuccess {
                sender.send(DCResource(resourceUri), project, type, BindingKeyAction.DELETE)
            }
    }

    fun exists(project: String, type: String, iri: String, bearer: String): Mono<Boolean> {
        // If dcResourceIri already encoded return the decoded version to avoid % encoding to %25
        // e.g. "some iri" -> "some%20iri" -> "some%2520iri"
        val resourceUri = checkEncoding(dcResourceUri(type, iri))
        val encodedUri = UriComponentsBuilder.fromUriString(resourceUri).build().encode().toUriString()

        logger.debug("Checking existence of resource $encodedUri")

        return WebClient.create().get()
            .uri(encodedUri)
            .header("X-Datacore-Project", project)
            .header("Authorization", "Bearer $bearer")
            .exchange()
            .flatMap { Mono.just(true) }
            .onErrorResume { Mono.just(false) }
    }

    fun getResourceFromIRI(project: String, type: String, iri: String, bearer: String): Mono<DCResource> {
        // If dcResourceIri already encoded return the decoded version to avoid % encoding to %25 ("some iri" -> "some%20iri" -> "some%2520iri")
        val resourceUri = checkEncoding(dcResourceUri(type, iri))

        logger.debug("Fetching resource from URI $resourceUri")

        return WebClient.create().get()
            .uri(resourceUri)
            .header("X-Datacore-Project", project)
            .header("Authorization", "Bearer $bearer")
            .retrieve()
            .onStatus(HttpStatus::is4xxClientError, this::unwrapDatacoreError)
            .bodyToMono(DCResource::class.java)
    }

    private fun checkEncoding(iri: String): String {
        val decodedIri = URLDecoder.decode(iri, "UTF-8")
        return if (decodedIri.length < iri.length) {
            decodedIri
        } else {
            iri
        }
    }

    fun findResources(project: String, type: String, queryParameters: MultiValueMap<String, String>, start: Int = 0, maxResult: Int = 100, bearer: String): Flux<DCResource> {

        val startParam = queryParameters.getFirst("start") ?: start
        val limitParam = queryParameters.getFirst("limit") ?: maxResult
        val uriComponentsBuilder = UriComponentsBuilder.fromUriString(datacoreProperties.url)
            .path("/dc/type/{type}")
            .queryParam("start", startParam)
            .queryParam("limit", limitParam)

        queryParameters.forEach { (k, v) ->
            if (k != "start" && k != "limit")
                uriComponentsBuilder.queryParam(k, v)
        }

        val uriComponents = uriComponentsBuilder
            .build()
            .expand(type)
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

        if (logger.isDebugEnabled) {
            logger.debug("Fetching limited Resources: URI String is $requestUri")
        }

        return WebClient.create(requestUri).get()
            .header("X-Datacore-Project", project)
            .header("Authorization", "Bearer $bearer")
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .onStatus(HttpStatus::is4xxClientError, this::unwrapDatacoreError)
            .bodyToFlux(DCResource::class.java)
    }

    fun findResources(project: String, type: String, queryParameters: DCQueryParameters, start: Int = 0, maxResult: Int = 100, bearer: String): Flux<DCResource> {

        val parametersMap = LinkedMultiValueMap<String, String>()
        queryParameters.forEach {
            parametersMap.add(it.subject, it.operator.value + it.getObject())
        }

        return findResources(project, type, parametersMap, start, maxResult, bearer)
    }

    fun findModels(limit: Int, name: String, bearer: String): Flux<DCModel> {

        val uriComponentsBuilder = UriComponentsBuilder.fromUriString(datacoreProperties.url)
            .path("/dc/type/dcmo:model_0")
            .queryParam("limit", limit)
            .queryParam("dcmo:name", "${DCOperator.REGEX.value}$name")

        val uriComponents = uriComponentsBuilder
            .build()
            .encode()

        val requestUri = uriComponents.toUriString()

        return WebClient.create(requestUri).get()
            .header("Authorization", "Bearer $bearer")
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .onStatus(HttpStatus::is4xxClientError, this::unwrapDatacoreError)
            .bodyToFlux(DCModel::class.java)
    }

    fun findModel(type: String, bearer: String): Mono<DCModel> {

        val uri = UriComponentsBuilder.fromUriString(datacoreProperties.url)
            .path("/dc/type/dcmo:model_0/{type}")
            .build()
            .expand(type)
            .encode()
            .toUriString()

        logger.debug("Fetching model, URI String is {}", uri)

        return WebClient.create(uri).get()
            .header("Authorization", "Bearer $bearer")
            .header("X-Datacore-Project", "oasis.main")
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .onStatus(HttpStatus::is4xxClientError, this::unwrapDatacoreError)
            .bodyToMono(DCModel::class.java)
    }

    private fun encodedUrlToType(type: String): String =
        UriComponentsBuilder.fromUriString(datacoreProperties.url)
            .path("/dc/type/{type}")
            .build()
            .expand(type)
            .encode() // ex. orgprfr:OrgPriv%C3%A9e_0 (WITH unencoded ':' and encoded accented chars etc.)
            .toUriString()

    private fun dcResourceUri(type: DCModelType, iri: String): String =
        StringBuilder(datacoreProperties.url)
            .append(datacoreProperties.typePrefix)
            .append('/')
            .append(type.encodeUriPathSegment())
            .append('/')
            .append(iri) // already encoded
            .toString()

    fun unwrapDatacoreError(clientResponse: ClientResponse): Mono<Throwable> =
        clientResponse.bodyToMono(String::class.java)
            .flatMap {
                logger.error("Got error response from Datacore: $it")
                Mono.just(HttpClientErrorException(clientResponse.statusCode(), clientResponse.statusCode().reasonPhrase,
                    it.toByteArray(), Charsets.UTF_8))
            }
}
