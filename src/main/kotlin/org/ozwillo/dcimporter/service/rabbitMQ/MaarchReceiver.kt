package org.ozwillo.dcimporter.service.rabbitMQ

import org.ozwillo.dcimporter.model.datacore.DCBusinessResourceLight
import org.ozwillo.dcimporter.service.MaarchService
import org.ozwillo.dcimporter.util.BindingKeyAction
import org.ozwillo.dcimporter.util.JsonConverter
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.amqp.core.Message
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class MaarchReceiver(private val maarchService: MaarchService) {

    private val logger: Logger = LoggerFactory.getLogger(MaarchReceiver::class.java)

    @Value("\${publik.datacore.modelEM}")
    private val elecmeetingType = "citizenreq:elecmeeting_0"

    @RabbitListener(queues = ["maarch"])
    @Throws(InterruptedException::class)
    fun receive(incoming: Message) {
        val message = String(incoming.body)
        val routingKey = incoming.messageProperties.receivedRoutingKey
        val resource = JsonConverter.jsonToobject(message)

        routingByBindingKey(resource, routingKey)
    }

    fun routingByBindingKey(resource: DCBusinessResourceLight, routingKey: String) {
        when {
            routingBindingKeyOfAction(routingKey, BindingKeyAction.CREATE) ->
                when {
                    routingBindingKeyOfType(routingKey, elecmeetingType) -> {
                        logger.debug("Binding $routingKey received elecmeeting request ${resource.getUri()}")
                        maarchService.createCitizenRequest(resource)
                    }

                    else -> logger.warn("Unable to recognize type from routing key $routingKey")
                }

            else -> logger.warn("Unable to recognize action (create) from routing key $routingKey")
        }
    }

    fun routingBindingKeyOfAction(routingKey: String, controlKey : BindingKeyAction): Boolean {
        return routingKey.substringAfterLast('.') == controlKey.value
    }

    fun routingBindingKeyOfType(routingKey: String, type: String): Boolean {
        return routingKey.split('.')[2] == type
    }
}