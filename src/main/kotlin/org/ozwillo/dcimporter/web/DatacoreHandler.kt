package org.ozwillo.dcimporter.web

import org.ozwillo.dcimporter.model.datacore.DCModel
import org.ozwillo.dcimporter.model.datacore.DCOperator
import org.ozwillo.dcimporter.model.datacore.DCOrdering
import org.ozwillo.dcimporter.model.datacore.DCQueryParameters
import org.ozwillo.dcimporter.model.datacore.DCResource
import org.ozwillo.dcimporter.model.sirene.Organization
import org.ozwillo.dcimporter.service.DatacoreService
import org.ozwillo.dcimporter.service.exceptions.BearerNotFoundException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.*
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.*
import org.springframework.web.reactive.function.server.bodyToMono
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

@Component
class DatacoreHandler(
    private val datacoreService: DatacoreService
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Value("\${datacore.model.modelORG}")
    private val modelOrg = ""

    fun getModels(req: ServerRequest): Mono<ServerResponse> {
        val bearer = extractBearer(req.headers())
        val name = req.queryParam("name").orElse("")

        return datacoreService.findModels(10, name, bearer)
            .reduce(listOf<DCModel>(), { acc, dcModel -> acc.plus(dcModel) })
            .flatMap {
                ok().bodyValue(it)
            }
            .onErrorResume { throwableToResponse(it) }
    }

    fun getModel(req: ServerRequest): Mono<ServerResponse> {
        val bearer = extractBearer(req.headers())
        val type = req.pathVariable("type")

        return datacoreService.findModel(type, bearer)
            .flatMap {
                ok().bodyValue(it)
            }
            .onErrorResume { throwableToResponse(it) }
    }

    fun getOrganization(req: ServerRequest): Mono<ServerResponse> {
        val bearer = extractBearer(req.headers())

        if (req.queryParam("name").isEmpty && req.queryParam("siret").isEmpty)
            return badRequest().bodyValue("Missing query parameter \"name\" or \"siret\"")
        else if (req.queryParam("name").isPresent && req.queryParam("siret").isPresent)
            return badRequest().bodyValue("Please provided one of \"name\" or \"siret\" parameters, not both")

        val dcQueryParameters =
            if (req.queryParam("name").isPresent)
                DCQueryParameters("org:legalName", DCOperator.REGEX, DCOrdering.DESCENDING, req.queryParam("name").get())
            else
                DCQueryParameters("org:regNumber", DCOperator.EQ, DCOrdering.DESCENDING, req.queryParam("siret").get())

        return datacoreService.findResources("oasis.main", modelOrg, dcQueryParameters, 0, 100, bearer)
            .map { Organization.fromDcObject(it) }
            .reduce(listOf<Organization>(), { acc, org -> acc.plus(org) })
            .flatMap {
                ok().bodyValue(it)
            }
            .onErrorResume { throwableToResponse(it) }
    }

    fun createResource(req: ServerRequest): Mono<ServerResponse> {
        val type = req.pathVariable("type")
        val project = extractProject(req.headers())
        val bearer = extractBearer(req.headers())

        return req.bodyToMono<DCResource>()
            .zipWhen {
                datacoreService.checkAndCreateLinkedResources(project, bearer, it)
            }
            .flatMap {
                datacoreService.saveResource(project, type, it.t1, bearer)
            }
            .flatMap {
                datacoreService.getResourceFromIRI(project, type, it.getIri(), bearer)
            }
            .flatMap {
                status(HttpStatus.CREATED).contentType(MediaType.APPLICATION_JSON).bodyValue(it)
            }
            .onErrorResume {
                throwableToResponse(it)
            }
    }

    fun updateResource(req: ServerRequest): Mono<ServerResponse> {
        val type = req.pathVariable("type")
        val project = extractProject(req.headers())
        val bearer = extractBearer(req.headers())

        return req.bodyToMono<DCResource>()
            .zipWhen {
                datacoreService.checkAndCreateLinkedResources(project, bearer, it)
            }
            .flatMap {
                datacoreService.updateResource(project, type, it.t1, bearer)
            }
            .flatMap {
                ok().build()
            }
            .onErrorResume {
                throwableToResponse(it)
            }
    }

    fun deleteResource(req: ServerRequest): Mono<ServerResponse> {
        val type = req.pathVariable("type")
        val project = extractProject(req.headers())
        val iri = req.pathVariable("iri")
        val bearer = extractBearer(req.headers())

        return Triple(project, type, iri).toMono()
            .flatMap {
                datacoreService.deleteResource(it.first, it.second, it.third, bearer)
            }
            .flatMap {
                if (it)
                    noContent().build()
                else
                    badRequest().build()
            }
            .onErrorResume {
                throwableToResponse(it)
            }
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
            throw BearerNotFoundException()

        return authorizationHeader[0].split(" ")[1]
    }

    private fun throwableToResponse(throwable: Throwable): Mono<ServerResponse> {
        logger.error("Operation failed with error $throwable")
        return when (throwable) {
            is HttpClientErrorException -> status(throwable.statusCode).bodyValue(throwable.responseBodyAsString)
            else -> badRequest().bodyValue(throwable.message.orEmpty())
        }
    }
}
