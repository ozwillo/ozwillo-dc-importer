package org.ozwillo.dcimporter.service.rabbitMQ

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.function.Executable
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.ozwillo.dcimporter.repository.BusinessMappingRepository
import org.ozwillo.dcimporter.service.MarcheSecuriseService
import org.ozwillo.dcimporter.util.BindingKeyAction
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest
class MarcheSecuriseReceiverTest {

    @Mock
    private lateinit var businessMappingRepository: BusinessMappingRepository

    @Mock
    private lateinit var marcheSecuriseService: MarcheSecuriseService

    @MockBean
    private lateinit var marcheSecuriseReceiver: MarcheSecuriseReceiver

    private val routingKeyExample = "marchepublic_0.25060187900043.marchepublic:consultation_0.create"

    private val consultationType = "marchepublic:consultation_0"

    private val lotType = "marchepublic:lot_0"

    @BeforeAll
    fun setUp(){
        MockitoAnnotations.initMocks(this)
        marcheSecuriseService = MarcheSecuriseService(businessMappingRepository)
        marcheSecuriseReceiver  = MarcheSecuriseReceiver(marcheSecuriseService)
    }


    @Test
    fun `Assert return of routing of actions binding key`() {
        assertAll("action",
                Executable { assertTrue(marcheSecuriseReceiver.routingBindingKeyOfAction(routingKeyExample, BindingKeyAction.CREATE))},
                Executable { assertFalse(marcheSecuriseReceiver.routingBindingKeyOfAction(routingKeyExample, BindingKeyAction.UPDATE))},
                Executable { assertFalse(marcheSecuriseReceiver.routingBindingKeyOfAction(routingKeyExample, BindingKeyAction.DELETE))}
        )
    }

    @Test
    fun `Assert return of routing of types binding keys`() {
        assertAll("type",
                Executable { assertTrue(marcheSecuriseReceiver.routingBindingKeyOfType(routingKeyExample, consultationType))},
                Executable { assertFalse(marcheSecuriseReceiver.routingBindingKeyOfType(routingKeyExample, lotType))}
        )
    }
}