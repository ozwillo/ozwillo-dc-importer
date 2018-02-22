package org.ozwillo.dcimporter

import org.apache.http.HttpStatus
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.ozwillo.dcimporter.model.publik.FormModel
import org.ozwillo.dcimporter.model.publik.Submission
import org.ozwillo.dcimporter.model.publik.User
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.test.test

@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PostAndNotifyTest : AbstractIntegrationTests() {

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
        client.get().uri("/api/status").retrieve().bodyToMono<String>()
                .test()
                .consumeNextWith {
                    assertThat(it).isEqualToIgnoringCase("ok")
                }
                .verifyComplete()
        client.post().uri("/api/publik/form").contentType(MediaType.APPLICATION_JSON).syncBody(formModel).exchange().test()
//                .consumeNextWith {
//                    assertThat(it.title).isEqualTo("Reactor Bismuth is out")
//                    assertThat(it.headline).startsWith("It is my great pleasure to")
//                    assertThat(it.content).startsWith("With the release of")
//                    assertThat(it.addedAt).isEqualTo(LocalDateTime.of(2017, 9, 28, 12, 0))
//                    assertThat(it.author).isEqualTo("simonbasle")
//                }
                .consumeNextWith {
                    assertThat(it.statusCode()).isEqualTo(HttpStatus.SC_OK)
                }
                .verifyComplete()
    }

}