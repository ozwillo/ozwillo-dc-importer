package org.ozwillo.dcimporter.handler

import com.ninjasquad.springmockk.MockkBean
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.verify
import java.net.URLEncoder
import java.nio.charset.Charset
import org.junit.jupiter.api.Test
import org.ozwillo.dcimporter.model.datacore.DCResource
import org.ozwillo.dcimporter.service.DatacoreService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.client.HttpClientErrorException
import reactor.core.publisher.Mono

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
class DatacoreHandlerTest {

    @Autowired
    private lateinit var webClient: WebTestClient

    @MockkBean
    private lateinit var datacoreService: DatacoreService

    @Test
    fun `it should return a 401 if Authorization header is missing`() {

        val expectedErrorMessage = """
            {
              "message": "Missing Authorization header in request"
            }
        """.trimIndent()

        webClient.get()
            .uri("/dc/models")
            .exchange()
            .expectStatus().isUnauthorized
            .expectBody().json(expectedErrorMessage)
    }

    @Test
    fun `it should ask to create unknown organization then create the resource`() {

        val resourceUri = "http://data.ozwillo.com/dc/type/grant:association_0/FR/1234/4567"
        val grantAssociationResource = """
            {
                "@id": "$resourceUri"
            }
        """.trimIndent()
        every { datacoreService.checkAndCreateLinkedResources(any(), any(), any()) } answers { Mono.just(emptyList()) }
        every { datacoreService.saveResource(any(), any(), any(), any()) } answers { Mono.just(DCResource(resourceUri, emptyMap())) }
        every { datacoreService.getResourceFromIRI(any(), any(), any(), any()) } answers { Mono.just(DCResource("", emptyMap())) }

        webClient.post()
            .uri("/dc/type/grant:association_0")
            .header("X-Datacore-Project", "grant_0")
            .header("Authorization", "Bearer mybearer")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(grantAssociationResource)
            .exchange()
            .expectStatus().isCreated

        verify { datacoreService.checkAndCreateLinkedResources(eq("grant_0"), eq("mybearer"),
            match { dcResource -> dcResource.getUri() == resourceUri }) }
        verify { datacoreService.saveResource(eq("grant_0"), eq("grant:association_0"), any(), eq("mybearer")) }
        verify { datacoreService.getResourceFromIRI(eq("grant_0"), eq("grant:association_0"),
            eq("FR/1234/4567"), eq("mybearer")) }

        confirmVerified()
    }

    @Test
    fun `it should throw a 401 error if bearer is expired when creating a resource`() {

        val resourceUri = "http://data.ozwillo.com/dc/type/grant:association_0/FR/1234/4567"
        val grantAssociationResource = """
            {
                "@id": "$resourceUri"
            }
        """.trimIndent()
        every { datacoreService.checkAndCreateLinkedResources(any(), any(), any()) } throws HttpClientErrorException(HttpStatus.UNAUTHORIZED)

        webClient.post()
            .uri("/dc/type/grant:association_0")
            .header("X-Datacore-Project", "grant_0")
            .header("Authorization", "Bearer mybearer")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(grantAssociationResource)
            .exchange()
            .expectStatus().isUnauthorized

        verify { datacoreService.checkAndCreateLinkedResources(eq("grant_0"), eq("mybearer"), any()) }

        confirmVerified()
    }

    @Test
    fun `it should ask to create unknown organization then update the resource`() {

        val resourceUri = "http://data.ozwillo.com/dc/type/grant:association_0/FR/1234/4567"
        val grantAssociationResource = """
            {
                "@id": "$resourceUri"
            }
        """.trimIndent()
        every { datacoreService.checkAndCreateLinkedResources(any(), any(), any()) } answers { Mono.just(emptyList()) }
        every { datacoreService.updateResource(any(), any(), any(), any()) } answers { Mono.just(HttpStatus.OK) }

        webClient.put()
            .uri("/dc/type/grant:association_0")
            .header("X-Datacore-Project", "grant_0")
            .header("Authorization", "Bearer mybearer")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(grantAssociationResource)
            .exchange()
            .expectStatus().isOk

        verify { datacoreService.checkAndCreateLinkedResources(eq("grant_0"), eq("mybearer"),
            match { dcResource -> dcResource.getUri() == resourceUri }) }
        verify { datacoreService.updateResource(eq("grant_0"), eq("grant:association_0"), any(), eq("mybearer")) }

        confirmVerified()
    }

    @Test
    fun `it should return a 204 if resource has been deleted`() {

        every { datacoreService.deleteResource(any(), any(), any(), any()) } answers { Mono.just(true) }

        val encodedIri = URLEncoder.encode("FR/1234/4567", Charset.forName("UTF-8"))
        webClient.delete()
            .uri("/dc/type/grant:association_0/$encodedIri")
            .header("X-Datacore-Project", "grant_0")
            .header("Authorization", "Bearer mybearer")
            .exchange()
            .expectStatus().isNoContent

        verify { datacoreService.deleteResource(eq("grant_0"), eq("grant:association_0"),
            eq("FR%2F1234%2F4567"), eq("mybearer")) }

        confirmVerified()
    }

    @Test
    fun `it should return a 400 if resource could not be deleted`() {

        every { datacoreService.deleteResource(any(), any(), any(), any()) } answers { Mono.just(false) }

        val encodedIri = URLEncoder.encode("FR/1234/4567", Charset.forName("UTF-8"))
        webClient.delete()
            .uri("/dc/type/grant:association_0/$encodedIri")
            .header("X-Datacore-Project", "grant_0")
            .header("Authorization", "Bearer mybearer")
            .exchange()
            .expectStatus().isBadRequest

        verify { datacoreService.deleteResource(eq("grant_0"), eq("grant:association_0"),
            eq("FR%2F1234%2F4567"), eq("mybearer")) }

        confirmVerified()
    }

    @Test
    fun `it should return a 404 if resource to be deleted does not exist`() {

        every { datacoreService.deleteResource(any(), any(), any(), any()) } throws HttpClientErrorException(HttpStatus.NOT_FOUND)

        val encodedIri = URLEncoder.encode("FR/1234/4567", Charset.forName("UTF-8"))
        webClient.delete()
            .uri("/dc/type/grant:association_0/$encodedIri")
            .header("X-Datacore-Project", "grant_0")
            .header("Authorization", "Bearer mybearer")
            .exchange()
            .expectStatus().isNotFound

        verify { datacoreService.deleteResource(eq("grant_0"), eq("grant:association_0"),
            eq("FR%2F1234%2F4567"), eq("mybearer")) }

        confirmVerified()
    }
}
