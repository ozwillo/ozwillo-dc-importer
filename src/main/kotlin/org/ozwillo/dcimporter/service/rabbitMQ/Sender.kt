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
    private val jsonConverter: JsonConverter? = null

    @Autowired
    private val topic: TopicExchange? = null

    @Throws(InterruptedException::class, AmqpException::class)
    fun sendCreate(resource: DCResourceLight, type: String, action: String) {

        val URI = resource.getUri()

        val SIRET = URI.split("/").get(7)
        if(SIRET.length != 9){
            LOGGER.error("Improper siret {}", SIRET)
        }

        val KEY = getKey(type, SIRET, action)

        val message = jsonConverter!!.objectToJson(resource)
        LOGGER.debug("conversion : {}", resource)

        template!!.convertAndSend(topic!!.name, KEY, message)

        LOGGER.debug("message sent with routing key : {}", KEY)
    }

    private fun getKey(type: String, uri: String, action: String): String {
        return "$type.$uri.$action"
    }
}