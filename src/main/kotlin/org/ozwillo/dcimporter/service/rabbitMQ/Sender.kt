package org.ozwillo.dcimporter.service.rabbitMQ

import org.ozwillo.dcimporter.model.datacore.DCResource
import org.ozwillo.dcimporter.util.BindingKeyAction
import org.ozwillo.dcimporter.util.JsonConverter
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.amqp.AmqpException
import org.springframework.amqp.core.MessageBuilder
import org.springframework.amqp.core.MessageProperties
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.*

@Service
class Sender(private val template: RabbitTemplate) {

    private val logger: Logger = LoggerFactory.getLogger(Sender::class.java)

    @Value("\${amqp.config.exchangerName}")
    private val exchangerName = ""

    @Throws(InterruptedException::class, AmqpException::class)
    fun send(resource: DCResource, project: String, type: String, action: BindingKeyAction) {

        val key = composeKey(project, type, action)
        val message = JsonConverter.objectToJson(resource)

        logger.debug("About to send $message with key $key")

        val properties = MessageProperties()
        properties.setHeader("original-routing-key", key)
        properties.setHeader("message-id", UUID.randomUUID())

        template.convertAndSend(
            exchangerName,
            key,
            MessageBuilder.withBody(message.toByteArray()).andProperties(properties).build()
        )
    }

    fun composeKey(project: String, type: String, action: BindingKeyAction): String {
        return "$project.$type.${action.value}"
    }
}
