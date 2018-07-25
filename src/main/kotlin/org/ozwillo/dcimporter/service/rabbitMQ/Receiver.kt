package org.ozwillo.dcimporter.service.rabbitMQ

import org.ozwillo.dcimporter.model.marchepublic.Consultation
import org.ozwillo.dcimporter.model.marchepublic.Lot
import org.ozwillo.dcimporter.model.marchepublic.Piece
import org.ozwillo.dcimporter.service.MarcheSecuriseService
import org.ozwillo.dcimporter.util.JsonConverter
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.amqp.core.Message
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.beans.factory.annotation.Value

class Receiver (val marcheSecuriseService: MarcheSecuriseService) {

    private val logger:Logger = LoggerFactory.getLogger(Receiver::class.java)

    @Value("\${marchesecurise.url.createConsultation}")
    private val createConsultationUrl = ""
    @Value("\${marchesecurise.url.updateConsultation}")
    private val updateConsultationUrl = ""
    @Value("\${marchesecurise.url.deleteConsultation}")
    private val deleteConsultationUrl = ""
    @Value("\${marchesecurise.url.lot}")
    private val lotUrl = ""
    @Value("\${marchesecurise.url.piece}")
    private val pieceUrl = ""

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
        val iri:String = resource.getIri()

        var response = ""

        // Consultation
        if (routingKey.contains("marchepublic:consultation_0")){

            // Creation
            if (routingKey.contains("create")){
                val consultation:Consultation = Consultation.toConsultation(resource)
                logger.debug("Queue marchesecurise (binding key {}) received consultation {}", bindingKey, consultation)
                val soapResponse = marcheSecuriseService.createAndUpdateConsultation(login, password, pa, consultation, createConsultationUrl)
                response = "SOAP sending, response : $soapResponse"
            }
            // Update
            else if (routingKey.contains("update")){
                val consultation:Consultation = Consultation.toConsultation(resource)
                logger.debug("Queue marchesecurise (binding key {}) received consultation {}", bindingKey, consultation)
                val soapResponse = marcheSecuriseService.updateConsultation(login, password, pa, consultation, updateConsultationUrl)
                response = "SOAP sending, response : $soapResponse"
            }
            // Delete
            else if (routingKey.contains("delete")){
                val soapResponse = marcheSecuriseService.deleteConsultation(login, password, pa, iri, deleteConsultationUrl)
                response = "SOAP sending, response : $soapResponse"
            }
            // Any of them
            else{
                logger.error("Unable to recognize requested action (create, update or delete) for type consultation from routing key {}", routingKey)
            }

        // Lot
        } else if(routingKey.contains("marchepublic:lot_0")) {
            //Create
            if (routingKey.contains("create")) {
                val lot: Lot = Lot.toLot(resource)
                logger.debug("binding 'marchepublic_0.#' received lot {}", lot)
                val response = marcheSecuriseService.createLot(login, password, pa, lot, uri, lotUrl)
                logger.debug("SOAP sending, response : {}", response)
            }
            //Any of them
            else {
                logger.error("Unable to recognize request (creation update or delete) from routing key{}", routingKey)
            }

            // Piece
        } else if (routingKey.contains("marchepublic:piece_0")){
            val piece:Piece = Piece.toPiece(resource)
            logger.debug("binding 'marchepublic_0.#' received piece {}", piece)

            // Create
            if (routingKey.contains("create")){
                val response = marcheSecuriseService.createPiece(login, password, pa, piece, uri, pieceUrl)
                logger.debug("SOAP sending, response : {}", response)
            }
            // Update
            else if(routingKey.contains("update")){
                val response = marcheSecuriseService.updatePiece(login, password, pa, piece, uri, pieceUrl)
                logger.debug("SOAP sending, response : {}", response)
            }
            // Any of them
            else{
                logger.error("Unable to recognize request (creation update or delete) from routing key{}", routingKey)
            }
        }
        logger.debug(response)
    }
}