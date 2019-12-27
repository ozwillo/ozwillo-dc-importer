package org.ozwillo.dcimporter.service.rabbitMQ

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.util.*
import org.ozwillo.dcimporter.config.ApplicationProperties
import org.ozwillo.dcimporter.model.datacore.DCResource
import org.slf4j.LoggerFactory
import org.springframework.amqp.core.MessageBuilder
import org.springframework.amqp.core.MessageProperties
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Service

@Service
class IoTSender(
    private val template: RabbitTemplate,
    private val applicationProperties: ApplicationProperties
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    fun send(resource: DCResource) {
        val mapper = jacksonObjectMapper()
        val message = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(resource)

        val key = resource.getStringValue("iotmeasure:type")

        logger.debug("About to send $message with key $key in topic ${applicationProperties.amqp.defaultExchangerName}")

        val properties = MessageProperties()
        properties.setHeader("original-routing-key", key)
        properties.setHeader("message-id", UUID.randomUUID())

        template.convertAndSend(
            applicationProperties.amqp.defaultExchangerName,
            key,
            MessageBuilder.withBody(message.toByteArray()).andProperties(properties).build()
        )
    }
}
