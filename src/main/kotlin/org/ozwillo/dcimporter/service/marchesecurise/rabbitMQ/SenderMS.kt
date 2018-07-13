package org.ozwillo.dcimporter.service.marchesecurise.rabbitMQ

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
class SenderMS {

    private val LOGGER:Logger = LoggerFactory.getLogger(SenderMS::class.java)

    @Autowired
    private val template: RabbitTemplate? = null

    @Autowired
    private val topic: TopicExchange? = null

    @Throws(InterruptedException::class, AmqpException::class)
    fun sendCreate(resource: DCResourceLight, type: String, action: String) {

        val URI = resource.getUri()

        val SIRET = URI.split("/").get(7)
        if(SIRET.length != 9){
            LOGGER.error("Siret {} non conforme", SIRET)
        }

        val KEY = getKey(type, SIRET, action)

        val message = JsonConverter.objectToJson(resource)
        LOGGER.debug("conversion consultation : {}", resource)

        template!!.convertAndSend(topic!!.name, KEY, message)

        LOGGER.debug("[RabbitMQ] message envoyé vers marchesecurise avec la clef : {}", KEY)
    }

    private fun getKey(type: String, uri: String, action: String): String {
        return "$type.$uri.$action"
    }
}