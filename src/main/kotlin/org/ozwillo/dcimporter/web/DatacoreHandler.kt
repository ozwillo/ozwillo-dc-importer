package org.ozwillo.dcimporter.web

import org.ozwillo.dcimporter.config.FullLoggingInterceptor
import org.ozwillo.dcimporter.model.datacore.DCBusinessResourceLight
import org.ozwillo.dcimporter.service.DatacoreService
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.web.reactive.error.DefaultErrorWebExceptionHandler
import org.springframework.http.*
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.ResourceAccessException
import org.springframework.web.client.RestTemplate
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.ok
import org.springframework.web.reactive.function.server.ServerResponse.status
import org.springframework.web.reactive.function.server.bodyToMono
import org.springframework.web.util.UriComponentsBuilder
import reactor.core.publisher.Mono
import sun.security.validator.ValidatorException
import java.net.URI

@Component
class DatacoreHandler (private val datacoreService: DatacoreService){

    companion object {
        private val logger = LoggerFactory.getLogger(DatacoreHandler::class.java)
    }

    fun createAndCheckOrCreateOrg(req: ServerRequest): Mono<ServerResponse> {
        val type = req.pathVariable("type")
        val project = extractProject(req.headers())
        val bearer = extractBearer(req.headers())

        return req.bodyToMono<DCBusinessResourceLight>()
                .flatMap { resource: DCBusinessResourceLight ->
                    val filteredResource = resource.getValues().filterValues { v -> v.toString().contains("http://data.ozwillo.com/dc/type/orgfr:Organisation_0") }
                    if (!filteredResource.isEmpty()){
                        filteredResource.forEach { key, value ->
                            val siret = value.toString().substringAfterLast("/")
                            try {
                                val dcOrg = datacoreService.getResourceFromIRI(project, "orgfr:Organisation_0", "FR/$siret", bearer)
                                logger.debug("Find organization $dcOrg")
                            }catch (e: HttpClientErrorException){
                                logger.debug("No organization dcObject found in datacore for siret $siret")
                                try {
                                    val dcOrg = getOrgFromSireneAPI(siret)
                                    logger.debug("Found organization $dcOrg on Insee database")
                                }catch (e: HttpClientErrorException){
                                    val body = when(e.statusCode){
                                        HttpStatus.UNAUTHORIZED -> "Token unauthorized, maybe it is expired ?"
                                        HttpStatus.NOT_FOUND -> "No organization found for siret $siret on Insee database"
                                        else -> "Unexpected error"
                                    }
                                    status(e.statusCode).body(BodyInserters.fromObject(body))
                                }
                            }
                        }
                        ok().contentType(MediaType.APPLICATION_JSON).body(BodyInserters.fromObject(filteredResource))
                    }else{
                        ok().contentType(MediaType.APPLICATION_JSON).body(BodyInserters.fromObject(resource.getValues()))
                    }
                }
    }

    private fun getOrgFromSireneAPI(siret:String): DCBusinessResourceLight{
        val encodedUri = UriComponentsBuilder.fromUriString("http://api.insee.fr/entreprises/sirene/V3/siret/$siret").build().encode().toUriString()
        val restTemplate = RestTemplate()

        val headers = LinkedMultiValueMap<String, String>()
        headers.set("Authorization", "Bearer xxxx")

        val request = RequestEntity<Any>(headers, HttpMethod.GET, URI(encodedUri))
        restTemplate.interceptors.add(FullLoggingInterceptor())
         return try {
            val response: ResponseEntity<DCBusinessResourceLight> = restTemplate.exchange(request, DCBusinessResourceLight::class.java)
            response.body!!
        }catch (e: ResourceAccessException){
            throw e;
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
}