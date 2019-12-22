package org.ozwillo.dcimporter.service.rabbitMQ

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.util.*
import org.ozwillo.dcimporter.model.datacore.DCResource
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.amqp.AmqpException
import org.springframework.amqp.core.MessageBuilder
import org.springframework.amqp.core.MessageProperties
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class IoTSender(private val template: RabbitTemplate) {

    private val logger: Logger = LoggerFactory.getLogger(IoTSender::class.java)

    @Value("\${amqp.config.defaultExchangerName}")
    private val exchangerName = ""

    @Throws(InterruptedException::class, AmqpException::class)
    fun send(resource: DCResource) {
        val mapper = jacksonObjectMapper()
        val message = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(resource)

        val key = resource.getStringValue("iotmeasure:type")

        logger.debug("About to send $message with key $key in topic $exchangerName")

        val properties = MessageProperties()
        properties.setHeader("original-routing-key", key)
        properties.setHeader("message-id", UUID.randomUUID())

        template.convertAndSend(
            exchangerName,
            key,
            MessageBuilder.withBody(message.toByteArray()).andProperties(properties).build()
        )
    }
}
