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

    private val LOGGER:Logger = LoggerFactory.getLogger(ReceiverMS::class.java)

    @Autowired
    private lateinit var datacoreService:DatacoreService

    @Autowired
    private val template: RabbitTemplate? = null

    @Autowired
    private val topic: TopicExchange? = null

    @Throws(InterruptedException::class, AmqpException::class)
    fun send(ressource: DCResourceLight, bearer: String, action: String) {

        val SIRET = "20003019500115"  //TODO: get siret from ressource

        val KEY = getKey("consultation", SIRET, action)

        val consultation:Consultation = Consultation.toConsultation(datacoreService.getResourceFromURI("marchepublic_0", "marchepublic:consultation_0", ressource.getIri(), bearer))
        val message = JsonConverter.consultationToJson(consultation)

        template!!.convertAndSend(topic!!.name, KEY, message)

        LOGGER.debug("message envoyé vers {}", KEY)
    }

    fun getKey(type: String, siret: String, action: String): String {
        return "$type.$siret.$action"
    }
}