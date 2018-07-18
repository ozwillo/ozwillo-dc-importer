package org.ozwillo.dcimporter.service.rabbitMQ

import org.ozwillo.dcimporter.model.datacore.DCResourceLight
import org.ozwillo.dcimporter.util.JsonConverter
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.amqp.AmqpException
import org.springframework.amqp.core.TopicExchange
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class Sender {

    private val LOGGER:Logger = LoggerFactory.getLogger(Sender::class.java)

    @Autowired
    private val template: RabbitTemplate? = null

    @Autowired
    private val topic: TopicExchange? = null

    @Throws(InterruptedException::class, AmqpException::class)

    fun send(resource: DCResourceLight, project:String, type: String, action: String) {

        val URI = resource.getUri()

        val SIRET = URI.split("/")[7]

        val KEY = getKey(project, SIRET, type, action)

        val uri = resource.getUri()
        val siret = uri.split("/").get(7)
        val key = getKey(project, siret, type , action)
        val message = JsonConverter.objectToJson(resource)

        LOGGER.debug("Conversion : {}", resource)

        template!!.convertAndSend(topic!!.name, key, message)

        LOGGER.debug("Message sent with routing key : {}", key)
    }

    private fun getKey(project:String, type: String, siret: String, action: String): String {
        return "$project.$siret.$type.$action"
    }
}