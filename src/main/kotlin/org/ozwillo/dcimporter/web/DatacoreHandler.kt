package org.ozwillo.dcimporter.web

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.ozwillo.dcimporter.config.ApplicationProperties
import org.ozwillo.dcimporter.config.DatacoreProperties
import org.ozwillo.dcimporter.config.FullLoggingInterceptor
import org.ozwillo.dcimporter.model.datacore.DCBusinessResourceLight
import org.ozwillo.dcimporter.model.kernel.TokenResponse
import org.ozwillo.dcimporter.model.sirene.Organization
import org.ozwillo.dcimporter.model.sirene.Response
import org.ozwillo.dcimporter.service.DatacoreService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.*
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.*
import org.springframework.web.reactive.function.server.bodyToMono
import org.springframework.web.util.UriComponentsBuilder
import reactor.core.publisher.Mono
import java.net.URI

@Component
class DatacoreHandler (private val datacoreService: DatacoreService,
                       private val datacoreProperties: DatacoreProperties,
                       private val applicationProperties: ApplicationProperties){

    companion object {
        private val logger = LoggerFactory.getLogger(DatacoreHandler::class.java)
    }

    @Value("\${insee.api.sirene.baseUri}")
    private val baseUri = ""
    @Value("\${insee.api.sirene.tokenUrl}")
    private val tokenUrl = ""
    @Value("\${insee.api.sirene.siretUrl}")
    private val siretUrl = ""
    @Value("\${insee.api.sirene.siretParameters}")
    private val siretParameters = ""
    @Value("\${insee.api.sirene.secretClient}")
    private val secretClient = ""

    fun createResourceWithOrganization(req: ServerRequest): Mono<ServerResponse> {
        val type = req.pathVariable("type")
        val project = extractProject(req.headers())
        val bearer = extractBearer(req.headers())

        return req.bodyToMono<DCBusinessResourceLight>()
                .flatMap { resource: DCBusinessResourceLight ->
                    val filteredResource = resource.getValues().filterValues { v -> v.toString().contains("${datacoreProperties.baseUri}/orgfr:Organisation_0") }
                    if (!filteredResource.isEmpty()){
                        findOrCreateDCOrganization(project, bearer, filteredResource)
                        datacoreService.saveResource(project, type, resource, bearer)
                                .flatMap {result ->
                                    val savedResult = datacoreService.getResourceFromIRI(project, type, result.resource.getIri(), bearer)
                                    status(HttpStatus.CREATED).contentType(MediaType.APPLICATION_JSON).body(BodyInserters.fromObject(savedResult))
                                }
                    }else{
                        badRequest().body(BodyInserters.fromObject("No organization found in request ${resource.getValues()}"))
                    }
                }
                .onErrorResume { error ->
                    when{
                        error is HttpClientErrorException && error.statusCode == HttpStatus.UNAUTHORIZED -> status(error.statusCode).body(BodyInserters.fromObject("Token unauthorized, maybe it is expired ?"))
                        else -> this.throwableToResponse(error)
                    }
                }
    }

    fun updateResourceWithOrganization(req: ServerRequest): Mono<ServerResponse>{
        val type = req.pathVariable("type")
        val project = extractProject(req.headers())
        val bearer = extractBearer(req.headers())

        return req.bodyToMono<DCBusinessResourceLight>()
                .flatMap { resource: DCBusinessResourceLight ->
                    val filteredResource = resource.getValues().filterValues { v -> v.toString().contains("${datacoreProperties.baseUri}/orgfr:Organisation_0") }
                    if (!filteredResource.isEmpty()){
                        findOrCreateDCOrganization(project, bearer, filteredResource)
                        datacoreService.updateResource(project, type, resource, bearer)
                        ok().contentType(MediaType.APPLICATION_JSON).body(BodyInserters.empty<String>())
                    }else{
                        badRequest().body(BodyInserters.fromObject("No organization found in request ${resource.getValues()}"))
                    }
                }
                .onErrorResume { error ->
                    when{
                        error is HttpClientErrorException && error.statusCode == HttpStatus.UNAUTHORIZED -> status(error.statusCode).body(BodyInserters.fromObject("Token unauthorized, maybe it is expired ?"))
                        else -> this.throwableToResponse(error)
                    }
                }
    }

    private fun findOrCreateDCOrganization(project: String, bearer: String, filteredMap: Map<String, Any>){
        var dcOrg: DCBusinessResourceLight
        filteredMap.forEach { _, value ->
            val siret = value.toString().substringAfterLast("/")
            try {
                dcOrg = datacoreService.getResourceFromIRI(project, "orgfr:Organisation_0", "FR/$siret", bearer)
                logger.debug("Find organization $dcOrg")
            }catch (e: HttpClientErrorException){
                if(e.statusCode == HttpStatus.NOT_FOUND){
                    logger.debug("No organization dcObject found in datacore for siret $siret")
                    dcOrg = getOrgFromSireneAPI(siret)
                    logger.debug("Found organization $dcOrg on Insee database")
                    datacoreService.saveResource(project, "orgfr:Organisation_0", dcOrg, bearer)
                }else{
                    throw e
                }
            }
        }
    }

    private fun getSireneToken():String{
        val restTemplate = RestTemplate()

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_FORM_URLENCODED
        headers.set("Authorization", "Basic $secretClient")
        val map = LinkedMultiValueMap<String, String>()
        map.add("grant_type", "client_credentials")

        val request = HttpEntity<MultiValueMap<String, String>>(map, headers)

        val response :ResponseEntity<TokenResponse> = restTemplate.postForEntity("$baseUri$tokenUrl", request, TokenResponse::class.java)
        return response.body!!.accessToken!!
    }

    private fun getOrgFromSireneAPI(siret:String): DCBusinessResourceLight{
        val sireneToken = "Bearer "+getSireneToken()
        val encodedUri = UriComponentsBuilder.fromUriString("$baseUri$siretUrl/$siret$siretParameters").build().encode().toUriString()
        val restTemplate = RestTemplate()

        val headers = LinkedMultiValueMap<String, String>()
        headers.set("Authorization", sireneToken)

        try {
            val request = RequestEntity<Any>(headers, HttpMethod.GET, URI(encodedUri))
            restTemplate.interceptors.add(FullLoggingInterceptor())
            val response: ResponseEntity<String> = restTemplate.exchange(request, String::class.java)
            logger.debug(response.body!!)

            val mapper = jacksonObjectMapper()
            mapper.findAndRegisterModules()
            val ResponseObject = mapper.readValue(response.body, Response::class.java)
            val cp = ResponseObject.etablissement!!.adresse!!.cp!!
            val numero = ResponseObject.etablissement.adresse!!.numero
            val typeVoie = ResponseObject.etablissement.adresse.typeVoie
            val libelleVoie = ResponseObject.etablissement.adresse.libelleVoie
            val Org = Organization(cp = cp,
                    voie = "$numero $typeVoie $libelleVoie",
                    pays = "${datacoreProperties.baseUri}/geocofr:Pays_0/FR",
                    denominationUniteLegale = ResponseObject.etablissement.uniteLegale!!.denominationUniteLegale ?: ResponseObject.etablissement.uniteLegale.nomUniteLegale!!,
                    siret = ResponseObject.etablissement.siret!!)

            return Org.toDcObject(datacoreProperties.baseUri, siret)
        }catch (e: Throwable){
            throw e
        }
    }

    private fun extractProject(headers: org.springframework.web.reactive.function.server.ServerRequest.Headers): String {
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
        DatacoreHandler.logger.error("Operation failed with error $throwable")
        return when (throwable) {
            is HttpClientErrorException -> ServerResponse.badRequest().body(BodyInserters.fromObject(throwable.responseBodyAsString))
            else -> {
                ServerResponse.badRequest().body(BodyInserters.fromObject(throwable.message.orEmpty()))
            }
        }

    }
}