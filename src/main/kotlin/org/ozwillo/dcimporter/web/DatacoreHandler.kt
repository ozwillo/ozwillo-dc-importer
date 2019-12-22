package org.ozwillo.dcimporter.web

import org.ozwillo.dcimporter.config.DatacoreProperties
import org.ozwillo.dcimporter.model.datacore.DCModel
import org.ozwillo.dcimporter.model.datacore.DCOperator
import org.ozwillo.dcimporter.model.datacore.DCOrdering
import org.ozwillo.dcimporter.model.datacore.DCQueryParameters
import org.ozwillo.dcimporter.model.datacore.DCResource
import org.ozwillo.dcimporter.model.sirene.Organization
import org.ozwillo.dcimporter.service.DatacoreService
import org.ozwillo.dcimporter.service.InseeSireneService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.*
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.*
import org.springframework.web.reactive.function.server.bodyToMono
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toFlux

@Component
class DatacoreHandler(
    private val datacoreService: DatacoreService,
    private val datacoreProperties: DatacoreProperties,
    private val inseeSireneService: InseeSireneService
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Value("\${datacore.model.modelORG}")
    private val modelOrg = ""

    fun getModels(req: ServerRequest): Mono<ServerResponse> {

        val name = req.queryParam("name").orElse("")

        return datacoreService.findModels(10, name)
            .reduce(listOf<DCModel>(), { acc, dcModel -> acc.plus(dcModel) })
            .flatMap {
                ok().bodyValue(it)
            }
            .onErrorResume {
                when (it) {
                    is HttpClientErrorException -> status(it.statusCode).body(BodyInserters.fromValue(it.message!!))
                    else -> status(HttpStatus.INTERNAL_SERVER_ERROR).build()
                }
            }
    }

    fun getModel(req: ServerRequest): Mono<ServerResponse> {
        val type = req.pathVariable("type")

        return datacoreService.findModel(type)
            .flatMap {
                ok().bodyValue(it)
            }
            .onErrorResume {
                when (it) {
                    is HttpClientErrorException -> status(it.statusCode).body(BodyInserters.fromValue(it.message!!))
                    else -> status(HttpStatus.INTERNAL_SERVER_ERROR).build()
                }
            }
    }

    fun getOrganization(req: ServerRequest): Mono<ServerResponse> {

        if (req.queryParam("name").isEmpty && req.queryParam("siret").isEmpty)
            return badRequest().bodyValue("Missing query parameter \"name\" or \"siret\"")
        else if (req.queryParam("name").isPresent && req.queryParam("siret").isPresent)
            return badRequest().bodyValue("Please provided one of \"name\" or \"siret\" parameters, not both")

        val dcQueryParameters =
            if (req.queryParam("name").isPresent)
                DCQueryParameters("org:legalName", DCOperator.REGEX, DCOrdering.DESCENDING, req.queryParam("name").get())
            else
                DCQueryParameters("org:regNumber", DCOperator.EQ, DCOrdering.DESCENDING, req.queryParam("siret").get())

        return datacoreService.findResources("oasis.main", modelOrg, dcQueryParameters, 0, 100)
            .map { Organization.fromDcObject(it) }
            .reduce(listOf<Organization>(), { acc, org -> acc.plus(org) })
            .flatMap {
                ok().bodyValue(it)
            }
            .onErrorResume {
                when (it) {
                    is HttpClientErrorException -> status(it.statusCode).body(BodyInserters.fromValue(it.message!!))
                    else -> status(HttpStatus.INTERNAL_SERVER_ERROR).build()
                }
            }
    }

    fun createResourceWithOrganization(req: ServerRequest): Mono<ServerResponse> {
        val type = req.pathVariable("type")
        val project = extractProject(req.headers())
        val bearer = extractBearer(req.headers())

        return req.bodyToMono<DCResource>()
            .flatMap { resource: DCResource ->
                val filteredResource = resource.getValues()
                    .filterValues { v -> v.toString().contains("${datacoreProperties.baseResourceUri()}/$modelOrg") }
                if (filteredResource.isNotEmpty()) {
                    filteredResource.values.toFlux()
                        .map { organizationUri ->
                            findOrCreateDCOrganization(project, bearer, organizationUri as String)
                        }
                        .collectList()
                        .flatMap {
                            datacoreService.saveResource(project, type, resource, bearer)
                        }
                        .flatMap { dcResource ->
                            datacoreService.getResourceFromIRI(project, type, dcResource.getIri(), bearer)
                        }.flatMap {
                            status(HttpStatus.CREATED).contentType(MediaType.APPLICATION_JSON)
                                .body(BodyInserters.fromValue(it))
                        }
                } else {
                    badRequest().body(
                        BodyInserters.fromValue("No organization found in request ${resource.getValues()}"))
                }
            }
            .onErrorResume { error ->
                when {
                    error is HttpClientErrorException && error.statusCode == HttpStatus.UNAUTHORIZED ->
                        status(error.statusCode)
                            .body(BodyInserters.fromValue("Token unauthorized, maybe it is expired ?"))
                    else -> this.throwableToResponse(error)
                }
            }
    }

    fun updateResourceWithOrganization(req: ServerRequest): Mono<ServerResponse> {
        val type = req.pathVariable("type")
        val project = extractProject(req.headers())
        val bearer = extractBearer(req.headers())

        return req.bodyToMono<DCResource>()
            .flatMap { resource: DCResource ->
                val filteredResource = resource.getValues()
                    .filterValues { v -> v.toString().contains("${datacoreProperties.baseResourceUri()}/$modelOrg") }
                if (filteredResource.isNotEmpty()) {
                    filteredResource.values.toFlux()
                        .map { organizationUri ->
                            findOrCreateDCOrganization(project, bearer, organizationUri as String)
                        }
                        .collectList()
                        .flatMap {
                            datacoreService.updateResource(project, type, resource, bearer)
                        }.flatMap {
                            ok().contentType(MediaType.APPLICATION_JSON).body(BodyInserters.empty<String>())
                        }
                } else {
                    badRequest().body(
                        BodyInserters.fromValue("No organization found in request ${resource.getValues()}"))
                }
            }
            .onErrorResume { error ->
                when {
                    error is HttpClientErrorException && error.statusCode == HttpStatus.UNAUTHORIZED -> status(
                        error.statusCode).body(
                        BodyInserters.fromValue("Token unauthorized, maybe it is expired ?")
                    )
                    else -> this.throwableToResponse(error)
                }
            }
    }

    private fun findOrCreateDCOrganization(project: String, bearer: String, organizationUri: String): Mono<DCResource> {

        val siret = organizationUri.substringAfterLast("/")

        return datacoreService.exists(project, modelOrg, "FR/$siret", bearer)
            .filter { it == false }
            .map { inseeSireneService.getOrgFromSireneAPI(siret) }
            .flatMap { datacoreService.saveResource(project, modelOrg, it, bearer) }
    }

    private fun extractProject(headers: ServerRequest.Headers): String {
        val project = headers.header("X-Datacore-Project")
        if (project.isEmpty() || project.size > 1)
            return ""

        return project[0]
    }

    private fun extractBearer(headers: ServerRequest.Headers): String {
        val authorizationHeader = headers.header("Authorization")
        if (authorizationHeader.isEmpty() || authorizationHeader.size > 1)
            return ""

        return authorizationHeader[0].split(" ")[1]
    }

    private fun throwableToResponse(throwable: Throwable): Mono<ServerResponse> {
        logger.error("Operation failed with error $throwable")
        return when (throwable) {
            is HttpClientErrorException -> badRequest().body(BodyInserters.fromValue(throwable.responseBodyAsString))
            else -> {
                badRequest().body(BodyInserters.fromValue(throwable.message.orEmpty()))
            }
        }
    }
}
