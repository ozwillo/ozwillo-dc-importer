package org.ozwillo.dcimporter.handler

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import java.util.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.ozwillo.dcimporter.AbstractIntegrationTests
import org.ozwillo.dcimporter.model.BusinessAppConfiguration
import org.ozwillo.dcimporter.model.datacore.DCResource
import org.ozwillo.dcimporter.model.publik.FormModel
import org.ozwillo.dcimporter.model.publik.Submission
import org.ozwillo.dcimporter.model.publik.User
import org.ozwillo.dcimporter.repository.BusinessAppConfigurationRepository
import org.ozwillo.dcimporter.service.DatacoreService
import org.ozwillo.dcimporter.service.KernelService
import org.ozwillo.dcimporter.service.PublikService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import reactor.core.publisher.Mono
import reactor.kotlin.test.test

// TODO : make it better
@ExtendWith(MockKExtension::class)
@ActiveProfiles("test")
class PublikHandlerTest : AbstractIntegrationTests() {

    @Autowired
    private lateinit var businessAppConfigurationRepository: BusinessAppConfigurationRepository

    @MockkBean
    private lateinit var datacoreService: DatacoreService

    @MockkBean
    private lateinit var kernelService: KernelService

    @BeforeAll
    fun declarePublikInstance() {
        val optPublikConfiguration = businessAppConfigurationRepository.findByOrganizationSiretAndApplicationName(
            "20003019500115",
            PublikService.name
        ).blockOptional()
        if (!optPublikConfiguration.isPresent) {
            val publikConfiguration = BusinessAppConfiguration(
                baseUrl = "https://demarches-sve.test-demarches.sictiam.fr",
                organizationSiret = "20003019500115", secretOrToken = "aSYZexOBIzl8", applicationName = "publik", displayName = "Publik"
            )
            businessAppConfigurationRepository.save(publikConfiguration).subscribe()
        }
    }

    @AfterAll
    fun deletePublikInstance() {
        businessAppConfigurationRepository.findByOrganizationSiretAndApplicationName(
            "20003019500115",
            PublikService.name
        ).doOnSuccess {
            businessAppConfigurationRepository.delete(it)
        }.subscribe()
    }

    @Test
    fun `Verify notification of a Publik form`() {
        val typeSlot = slot<String>()
        val iriSlot = slot<String>()
        every {
            kernelService.getAccessToken()
        } answers {
            Mono.just("mytoken")
        }
        every {
            datacoreService.getResourceFromIRI(any(), capture(typeSlot), capture(iriSlot), any())
        } answers {
            Mono.just(DCResource("http://data.ozwillo.com/dc/type/$typeSlot/$iriSlot"))
        }
        every {
            datacoreService.saveResource(
                project = eq("citizenreq_0"), type = "citizenreq:elecmeeting_0",
                resource = any(), bearer = any())
        } answers {
            Mono.just(
                    DCResource("http://data.ozwillo.com/dc/type/citizenreq:elecmeeting_0/FR/250601879/17-4")
            )
        }

        val formModel = FormModel(
            display_id = "17-4", last_update_time = "2017-05-11T09:08:54Z",
            display_name = "Demande de rendez-vous avec un élu - n°17-4",
            submission = Submission("web", false),
            url = "https://demarches-sve.test-demarches.sictiam.fr/demande-de-rendez-vous-avec-un-elu/4/",
            user = User(
                "admin@ozwillo-dev.eu", name = "agent_sictiam agent_sictiam",
                nameID = ArrayList(listOf("5c977a7f1d444fa1ab0f777325fdda93")), id = 1),
            criticality_level = 0, receipt_time = "2017-05-11T09:08:54Z",
            id = "demande-de-rendez-vous-avec-un-elu/3",
            workflowStatus = "New",
            fields = HashMap(mapOf(Pair("nom_famille", "agent_sictiam"))))

        client.post().uri("/api/publik/250601879/form")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(formModel)
            .exchange()
            .test()
            .consumeNextWith {
                assertThat(it.statusCode()).isEqualTo(HttpStatus.OK)
            }
            .verifyComplete()
    }
}
