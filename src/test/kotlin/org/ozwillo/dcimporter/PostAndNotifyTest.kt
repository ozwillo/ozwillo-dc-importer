package org.ozwillo.dcimporter

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.ozwillo.dcimporter.model.publik.FormModel
import org.ozwillo.dcimporter.model.publik.PublikConfiguration
import org.ozwillo.dcimporter.model.publik.Submission
import org.ozwillo.dcimporter.model.publik.User
import org.ozwillo.dcimporter.repository.PublikConfigurationRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import reactor.test.test

class PostAndNotifyTest : AbstractIntegrationTests() {

    @Autowired
    private lateinit var publikConfigurationRepository: PublikConfigurationRepository

    @BeforeAll
    fun declarePublikInstance() {
        val optPublikConfiguration = publikConfigurationRepository.findByDomain("demarches-sve.test-demarches.sictiam.fr").blockOptional()
        if (!optPublikConfiguration.isPresent) {
            val publikConfiguration = PublikConfiguration(domain = "demarches-sve.test-demarches.sictiam.fr",
                    organizationName = "SICTIAM", secret = "aSYZexOBIzl8")
            publikConfigurationRepository.save(publikConfiguration).subscribe()
        }
    }

    @AfterAll
    fun deletePublikInstance() {
        publikConfigurationRepository.findByDomain("demarches-sve.test-demarches.sictiam.fr").map {
            publikConfigurationRepository.delete(it).subscribe()
        }
    }

    @Test
    fun `Verify notification of a Publik form`() {
        val formModel = FormModel(display_id = "17-4", last_update_time = "2017-05-11T09:08:54Z",
                display_name = "Demande de rendez-vous avec un élu - n°17-4",
                submission = Submission("web", false),
                url = "https://demarches-sve.test-demarches.sictiam.fr/demande-de-rendez-vous-avec-un-elu/4/",
                user = User("admin@ozwillo-dev.eu", name = "agent_sictiam agent_sictiam",
                        nameID = ArrayList(listOf("5c977a7f1d444fa1ab0f777325fdda93")), id = 1),
                criticality_level = 0, receipt_time = "2017-05-11T09:08:54Z", id = "3", workflowStatus = "New",
                fields = HashMap(mapOf(Pair("nom_famille", "agent_sictiam"))))
        client.post().uri("/api/publik/form")
                .contentType(MediaType.APPLICATION_JSON)
                .syncBody(formModel)
                .exchange()
                .test()
                .consumeNextWith {
                    assertThat(it.statusCode()).isEqualTo(HttpStatus.OK)
                }
                .verifyComplete()
    }

}