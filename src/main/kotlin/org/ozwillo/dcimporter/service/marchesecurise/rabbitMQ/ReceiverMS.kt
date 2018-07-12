package org.ozwillo.dcimporter.service.marchesecurise.rabbitMQ

import org.ozwillo.dcimporter.model.marchepublic.Consultation
import org.ozwillo.dcimporter.model.marchepublic.Lot
import org.ozwillo.dcimporter.repository.BusinessMappingRepository
import org.ozwillo.dcimporter.service.marchesecurise.CreateConsultation
import org.ozwillo.dcimporter.service.marchesecurise.CreateOrModifyLot
import org.ozwillo.dcimporter.web.marchesecurise.MarcheSecuriseURL
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.amqp.core.Message
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.beans.factory.annotation.Autowired

class ReceiverMS (val businessMappingRepository: BusinessMappingRepository) {

    private val LOGGER:Logger = LoggerFactory.getLogger(ReceiverMS::class.java)

    //TODO:Externaliser identification
    private var login: String = "wsdev-sictiam"
    private var password: String = "WS*s1ctiam*"
    private var pa: String = "1267898337p8xft"


    @RabbitListener(queues = arrayOf("marchesecurise"))
    @Throws(InterruptedException::class)
    fun receive(incoming: Message) {

        val message = String(incoming.body)
        val routingKey = incoming.messageProperties.receivedRoutingKey

        val resource = JsonConverter.jsonToobject(message)
        val uri:String = resource.getUri()
        if (routingKey.contains("marchepublic:consultation_0") && routingKey.contains("create")){
            val consultation:Consultation = Consultation.toConsultation(resource)

            LOGGER.debug("[Rabbit Listener] 'consultation.siret.#' received consultation {}", consultation)

            val response = CreateConsultation.createAndModifyConsultation(login, password, pa, consultation, MarcheSecuriseURL.CREATE_CONSULTATION_URL, businessMappingRepository)
            LOGGER.debug("=== REPONSE ENVOI SOAP : ==\n {}", response)

        }else if (routingKey.contains("marchepublic:lot_0".toRegex())){
            val lot: Lot = Lot.toLot(resource)
            LOGGER.debug("[Rabbit Listener] 'consultation.siret.#' received lot {} with uri {}", lot, uri)
            val response = CreateOrModifyLot.createLot(login, password, pa, lot, uri, MarcheSecuriseURL.LOTS_URL, businessMappingRepository)
            LOGGER.debug("=== REPONSE ENVOI SOAP : ==\n {}", response)
        }
    }
}