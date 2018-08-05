package org.ozwillo.dcimporter.service.rabbitMQ

import org.ozwillo.dcimporter.model.datacore.DCResourceLight
import org.ozwillo.dcimporter.util.BindingKeyAction
import org.ozwillo.dcimporter.util.JsonConverter
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.amqp.AmqpException
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class Sender(private val template: RabbitTemplate) {

    private val logger:Logger = LoggerFactory.getLogger(Sender::class.java)

    @Value("\${amqp.config.exchangerName}")
    private val exchangerName = ""

    @Throws(InterruptedException::class, AmqpException::class)
    fun send(resource: DCResourceLight, project: String, type: String, action: BindingKeyAction) {

        val uri = resource.getUri()
        val siret = uri.split("/")[7]
        val key = getKey(project, type, siret , action)
        val message = JsonConverter.objectToJson(resource)

        logger.debug("Conversion : {}", resource)

        template.convertAndSend(exchangerName, key, message)

        logger.debug("Message sent with routing key : {}", key)
    }

    fun getKey(project: String, type: String, siret: String, action: BindingKeyAction): String {
        return "$project.$siret.$type.${action.value}"
    }
}