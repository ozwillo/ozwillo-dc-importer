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

    @Value("\${amqp.config.marchesecurise.bindingKey}")
    val bindingKey = ""

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
        var response = ""

     // Consultation
        if (routingKey.contains("marchepublic:consultation_0")){
            val consultation:Consultation = Consultation.toConsultation(resource)
            LOGGER.debug("Queue marchesecurise (binding key {}) received consultation {}", bindingKey, consultation)

         // Creation
            if (routingKey.contains("create")){
                val soapResponse = marcheSecuriseService.createAndUpdateConsultation(login, password, pa, consultation, CREATE_CONSULTATION_URL)
                response = "SOAP sending, response : $soapResponse"
            }
         // Update
            else if (routingKey.contains("update")){
                val soapResponse = marcheSecuriseService.updateConsultation(login, password, pa, consultation, UPDATE_CONSULTATION_URL)
                response = "SOAP sending, response : $soapResponse"
            }
         // Delete
            else if (routingKey.contains("delete")){
                val soapResponse = marcheSecuriseService.deleteConsultation(login, password, pa, consultation, DELETE_CONSULTATION_URL)
                response = "SOAP sending, response : $soapResponse"
            }
         // Any of them
            else{
                LOGGER.error("Unable to recognize requested action (create, update or delete) for type consultation from routing key {}", routingKey)
            }
        }
     // Lot
        else if (routingKey.contains("marchepublic:lot_0")){
            val lot: Lot = Lot.toLot(resource)
            LOGGER.debug("Queue marchesecurise (binding key {}) received lot {}", bindingKey, lot)
         // Create
            if(routingKey.contains("create")){
                val soapResponse = marcheSecuriseService.createLot(login, password, pa, lot, uri, LOT_URL)
                response = "SOAP sending, response : $soapResponse"
            }
         // Any of them
            else{
                LOGGER.error("Unable to recognize requested action (create, update or delete) for type consultation from routing key {}", routingKey)
            }
        }
     // Any of them
        else{
            LOGGER.error("Unable to recognize type (consultation, lot or piece) from routing key {}", routingKey)
        }
        LOGGER.debug(response)
    }
}