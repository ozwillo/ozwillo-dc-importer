package org.ozwillo.dcimporter.service.rabbitMQ

import org.ozwillo.dcimporter.model.marchepublic.Consultation
import org.ozwillo.dcimporter.model.marchepublic.Lot
import org.ozwillo.dcimporter.service.MarcheSecuriseService
import org.ozwillo.dcimporter.util.JsonConverter
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.amqp.core.Message
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.beans.factory.annotation.Value

class Receiver (val marcheSecuriseService: MarcheSecuriseService) {

    private val LOGGER:Logger = LoggerFactory.getLogger(Receiver::class.java)
    @Value("\${marchesecurise.url.createConsultation}")
    private val CREATE_CONSULTATION_URL = ""
    @Value("\${marchesecurise.url.updateConsultation}")
    private val UPDATE_CONSULTATION_URL = ""
    @Value("\${marchesecurise.url.deleteConsultation}")
    private val DELETE_CONSULTATION_URL = ""
    @Value("\${marchesecurise.url.lot}")
    private val LOT_URL = ""


    @Value("\${marchesecurise.login}")
    private var login: String = ""
    @Value("\${marchesecurise.password}")
    private var password: String = ""
    @Value("\${marchesecurise.pa}")
    private var pa: String = ""


    @RabbitListener(queues = arrayOf("marchesecurise"))
    @Throws(InterruptedException::class)
    fun receive(incoming: Message) {

        val message = String(incoming.body)
        val routingKey = incoming.messageProperties.receivedRoutingKey

        val resource = JsonConverter.jsonToobject(message)
        val uri:String = resource.getUri()
        if (routingKey.contains("marchepublic:consultation_0") && routingKey.contains("create")){
            val consultation:Consultation = Consultation.toConsultation(resource)

            LOGGER.debug("binding 'consultation.#' received consultation {}", consultation)

            val response = marcheSecuriseService.createAndUpdateConsultation(login, password, pa, consultation, CREATE_CONSULTATION_URL)
            LOGGER.debug("SOAP sending, response : {}", response)

        }else if (routingKey.contains("marchepublic:lot_0") && routingKey.contains("create")){
            val lot: Lot = Lot.toLot(resource)
            LOGGER.debug("binding 'consultation.#' received lot {}", lot)
            val response = marcheSecuriseService.createLot(login, password, pa, lot, uri, LOT_URL)
            LOGGER.debug("SOAP sending, response : {}", response)
        }
        else{
            LOGGER.error("Unable to recognize type (consultation, lot or piece from routing key {}", routingKey)
        }
    }
}