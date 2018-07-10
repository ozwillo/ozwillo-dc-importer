package org.ozwillo.dcimporter.service.marchesecurise.rabbitMQ

import org.ozwillo.dcimporter.model.datacore.DCBusinessResourceLight
import org.ozwillo.dcimporter.model.datacore.DCResourceLight
import org.ozwillo.dcimporter.model.marchepublic.Consultation
import org.ozwillo.dcimporter.service.DatacoreService
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
    fun send(resource: DCResourceLight, type: String, action: String) {

        val URI = resource.getUri()

        val KEY = getKey(type, URI, action)

        val consultation:Consultation = Consultation.toConsultation(resource as DCBusinessResourceLight)

        val message = JsonConverter.consultationToJson(consultation)
        LOGGER.debug("=======SENDER====== transformation consultation : {}", consultation)

        template!!.convertAndSend(topic!!.name, KEY, message)

        LOGGER.debug("[RabbitMQ] message envoyé vers marchesecurise avec la clef : {}", KEY)
    }

    private fun getKey(type: String, uri: String, action: String): String {
        return "$type.$uri.$action"
    }
}