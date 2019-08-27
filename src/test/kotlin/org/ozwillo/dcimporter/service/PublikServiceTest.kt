package org.ozwillo.dcimporter.service

import com.ninjasquad.springmockk.MockkBean
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.ozwillo.dcimporter.model.datacore.DCResource
import org.ozwillo.dcimporter.model.publik.FormModel
import org.ozwillo.dcimporter.model.publik.Submission
import org.ozwillo.dcimporter.model.publik.User
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import reactor.core.publisher.Mono
import java.util.ArrayList
import java.util.HashMap

@SpringBootTest
class PublikServiceTest {

    @Autowired
    private lateinit var publikService: PublikService

    @MockkBean
    private lateinit var datacoreService: DatacoreService

    @Test
    fun `it should call the DC to create a new user`() {
        every {
            datacoreService.getResourceFromIRI(any(), any(), any(), null)
        } answers {
            Mono.empty()
        }
        every {
            datacoreService.saveResource(any(), any(), any(), null)
        } answers {
            Mono.just(DCResource("http://new.user/1234"))
        }

        val formModel = FormModel(
            display_id = "17-4", last_update_time = "2017-05-11T09:08:54Z",
            display_name = "Demande de rendez-vous avec un élu - n°17-4",
            submission = Submission("web", false),
            url = "https://demarches-sve.test-demarches.sictiam.fr/demande-de-rendez-vous-avec-un-elu/4/",
            user = User(
                "admin@ozwillo-dev.eu", name = "agent_sictiam agent_sictiam",
                nameID = ArrayList(listOf("idofmyuser")), id = 1),
            criticality_level = 0, receipt_time = "2017-05-11T09:08:54Z",
            id = "demande-de-rendez-vous-avec-un-elu/3",
            workflowStatus = "New",
            fields = HashMap(mapOf(Pair("nom_famille", "agent_sictiam")))
        )

        publikService.getOrCreateUser(formModel)
            .subscribe {
                assertEquals(it, "http://new.user/1234")
            }

        verify {
            datacoreService.getResourceFromIRI("citizenreq_0", "citizenreq:user_0",
                match { iri -> iri == "idofmyuser" }, null)
        }
        verify {
            datacoreService.saveResource("citizenreq_0", "citizenreq:user_0",
                match { dcResource -> dcResource.getUri().endsWith("idofmyuser") }, null)
        }

        confirmVerified(datacoreService)
    }

    @Test
    fun `it should not call the DC to create a new user`() {
        every {
            datacoreService.getResourceFromIRI(any(), any(), any(), null)
        } answers {
            Mono.just(DCResource("http://new.user/1234"))
        }

        val formModel = FormModel(
            display_id = "17-4", last_update_time = "2017-05-11T09:08:54Z",
            display_name = "Demande de rendez-vous avec un élu - n°17-4",
            submission = Submission("web", false),
            url = "https://demarches-sve.test-demarches.sictiam.fr/demande-de-rendez-vous-avec-un-elu/4/",
            user = User(
                "admin@ozwillo-dev.eu", name = "agent_sictiam agent_sictiam",
                nameID = ArrayList(listOf("idofmyuser")), id = 1),
            criticality_level = 0, receipt_time = "2017-05-11T09:08:54Z",
            id = "demande-de-rendez-vous-avec-un-elu/3",
            workflowStatus = "New",
            fields = HashMap(mapOf(Pair("nom_famille", "agent_sictiam")))
        )

        publikService.getOrCreateUser(formModel)
            .subscribe {
                assertEquals(it, "http://new.user/1234")
            }

        verify {
            datacoreService.getResourceFromIRI("citizenreq_0", "citizenreq:user_0",
                match { iri -> iri == "idofmyuser" }, null)
        }

        confirmVerified(datacoreService)
    }
}