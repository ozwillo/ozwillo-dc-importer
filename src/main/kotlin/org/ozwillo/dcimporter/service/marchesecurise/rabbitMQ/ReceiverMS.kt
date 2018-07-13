package org.ozwillo.dcimporter.service.marchesecurise.rabbitMQ

import org.ozwillo.dcimporter.model.marchepublic.Consultation
import org.ozwillo.dcimporter.repository.BusinessMappingRepository
import org.ozwillo.dcimporter.service.marchesecurise.MarcheSecuriseService
import org.ozwillo.dcimporter.util.JsonConverter
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.amqp.core.Message
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.beans.factory.annotation.Value

class ReceiverMS (val businessMappingRepository: BusinessMappingRepository,
                  val marcheSecuriseService: MarcheSecuriseService) {

    private val LOGGER:Logger = LoggerFactory.getLogger(ReceiverMS::class.java)
    @Value("\${marchesecurise.config.url.createConsultation}")
    private val CREATE_CONSULTATION_URL = ""
    @Value("\${marchesecurise.config.url.updateConsultation}")
    private val UPDATE_CONSULTATION_URL = ""
    @Value("\${marchesecurise.config.url.deleteConsultation}")
    private val DELETE_CONSULTATION_URL = ""
    @Value("\${marchesecurise.config.url.lot}")
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

            LOGGER.debug("[Rabbit Listener] 'consultation.#' received consultation {}", consultation)

            val response = marcheSecuriseService.createAndUpdateConsultation(login, password, pa, consultation, CREATE_CONSULTATION_URL, businessMappingRepository)
            LOGGER.debug("envoi SOAP, réponse : {}", response)

        }/*else if (routingKey.contains("marchepublic:lot_0") && routingKey.contains("create")){
            val lot: Lot = Lot.toLot(resource)
            LOGGER.debug("[Rabbit Listener] 'consultation.#' received lot {} with uri {}", lot, uri)
            val response = marcheSecuriseService.createLot(login, password, pa, lot, uri, MarcheSecuriseURL.LOTS_URL, businessMappingRepository)
            LOGGER.debug("envoi SOAP, réponse : {}", response)
        }*/
        else{
            LOGGER.error("Type non reconnu")
        }
    }
}