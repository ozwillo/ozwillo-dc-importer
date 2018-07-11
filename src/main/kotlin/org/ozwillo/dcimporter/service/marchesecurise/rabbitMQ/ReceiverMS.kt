package org.ozwillo.dcimporter.service.marchesecurise.rabbitMQ

import org.ozwillo.dcimporter.model.BusinessMapping
import org.ozwillo.dcimporter.model.datacore.DCBusinessResourceLight
import org.ozwillo.dcimporter.model.datacore.DCResourceLight
import org.ozwillo.dcimporter.model.marchepublic.Consultation
import org.ozwillo.dcimporter.repository.BusinessMappingRepository
import org.ozwillo.dcimporter.service.DatacoreService
import org.ozwillo.dcimporter.service.marchesecurise.CreateConsultation
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
    fun receive(incoming: String) {
        val consultationMessage = JsonConverter.JsonToConsultation(incoming)
        val consultation:Consultation = consultationMessage.consultation
        val uri:String = consultationMessage.uri
        LOGGER.debug("[Rabbit Listener] 'consultation.siret.#' received consultation {} with uri {}", consultation, uri)

        val response = CreateConsultation.createAndModifyConsultation(login, password, pa, consultation, uri, MarcheSecuriseURL.CREATE_CONSULTATION_URL, businessMappingRepository)
        LOGGER.debug("=== REPONSE ENVOI SOAP : ==\n {}", response)
    }
}