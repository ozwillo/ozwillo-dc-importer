package org.ozwillo.dcimporter.service.marchesecurise.rabbitMQ

import org.ozwillo.dcimporter.model.marchepublic.Consultation
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.amqp.core.Message
import org.springframework.amqp.rabbit.annotation.RabbitListener

class ReceiverMS {

    private val LOGGER:Logger = LoggerFactory.getLogger(ReceiverMS::class.java)

    @RabbitListener(queues = arrayOf("marchesecurise"))
    @Throws(InterruptedException::class)
    fun receive(incoming: String) {
        val consultation:Consultation = JsonConverter.JsonToConsultation(incoming)
        LOGGER.debug("Rabbit Listener 'consultation.siret.#' received {} ", consultation)
    }
}