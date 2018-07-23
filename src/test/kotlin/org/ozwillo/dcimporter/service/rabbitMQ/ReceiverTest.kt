package org.ozwillo.dcimporter.service.rabbitMQ

import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.function.Executable
import org.ozwillo.dcimporter.service.MarcheSecuriseService
import org.ozwillo.dcimporter.util.BindingKeyAction
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest
class ReceiverTest {

    @MockBean
    private var marcheSecuriseService: MarcheSecuriseService = MarcheSecuriseService()

    @MockBean
    private var receiver: Receiver = Receiver(marcheSecuriseService)

    private val routingKeyExample = "marchepublic_0.25060187900043.marchepublic:consultation_0.create"

    private val consultationType = "marchepublic:consultation_0"

    private val lotType = "marchepublic:lot_0"

    @Test
    fun `Assert return of routing of actions binding key`() {
        assertAll("action",
                Executable { assertTrue(receiver.routingBindingKeyOfAction(routingKeyExample, BindingKeyAction.CREATE))},
                Executable { assertFalse(receiver.routingBindingKeyOfAction(routingKeyExample, BindingKeyAction.UPDATE))},
                Executable { assertFalse(receiver.routingBindingKeyOfAction(routingKeyExample, BindingKeyAction.DELETE))}
        )
    }

    @Test
    fun `Assert return of routing of types binding keys`() {
        assertAll("type",
                Executable { assertTrue(receiver.routingBindingKeyOfType(routingKeyExample, consultationType))},
                Executable { assertFalse(receiver.routingBindingKeyOfType(routingKeyExample, lotType))}
        )
    }
}