package org.ozwillo.dcimporter.service.rabbitMQ

import org.ozwillo.dcimporter.model.datacore.DCBusinessResourceLight
import org.ozwillo.dcimporter.model.marchepublic.Consultation
import org.ozwillo.dcimporter.model.marchepublic.Lot
import org.ozwillo.dcimporter.model.marchepublic.Piece
import org.ozwillo.dcimporter.service.MarcheSecuriseService
import org.ozwillo.dcimporter.util.BindingKeyAction
import org.ozwillo.dcimporter.util.JsonConverter
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.amqp.core.Message
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class MarcheSecuriseReceiver (val marcheSecuriseService: MarcheSecuriseService) {

    private val logger:Logger = LoggerFactory.getLogger(MarcheSecuriseReceiver::class.java)

    @Value("\${marchesecurise.url.createConsultation}")
    private val createConsultationUrl = ""
    @Value("\${marchesecurise.url.updateConsultation}")
    private val updateConsultationUrl = ""
    @Value("\${marchesecurise.url.deleteConsultation}")
    private val deleteConsultationUrl = ""
    @Value("\${marchesecurise.url.publishConsultation}")
    private val publishConsultationUrl = ""
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


    @RabbitListener(queues = ["marchesecurise"])
    @Throws(InterruptedException::class)
    fun receive(incoming: Message) {
        val message = String(incoming.body)
        val routingKey = incoming.messageProperties.receivedRoutingKey
        val resource = JsonConverter.jsonToObject(message)

        routingByBindingKey(resource, routingKey)
    }

    fun routingByBindingKey(resource: DCBusinessResourceLight, routingKey: String) {
        when {
            routingBindingKeyOfAction(routingKey, BindingKeyAction.CREATE) ->
                when {
                    routingBindingKeyOfType(routingKey,"marchepublic:consultation_0") -> {
                        val consultation:Consultation = Consultation.fromDCObject(resource)
                        logger.debug("Binding $routingKey received consultation ${consultation.objet}")
                        marcheSecuriseService.createAndUpdateConsultation(login, password, pa, consultation, resource.getUri(), createConsultationUrl)
                    }

                    routingBindingKeyOfType(routingKey, "marchepublic:lot_0") -> {
                        val lot: Lot = Lot.toLot(resource)
                        logger.debug("Binding $routingKey received lot ${lot.libelle}")
                        marcheSecuriseService.createLot(login, password, pa, lot, resource.getUri(), lotUrl)
                    }

                    routingBindingKeyOfType(routingKey, "marchepublic:piece_0") -> {
                        val piece:Piece = Piece.toPiece(resource)
                        logger.debug("Binding $routingKey received piece ${piece.libelle}")
                        marcheSecuriseService.createPiece(login, password, pa, piece, resource.getUri(), pieceUrl)
                    }

                    else -> logger.warn("Unable to recognize type (consultation, lot or piece) from routing key $routingKey")
                }

            routingBindingKeyOfAction(routingKey, BindingKeyAction.UPDATE) ->
                when {
                    routingBindingKeyOfType(routingKey,"marchepublic:consultation_0") -> {
                        val consultation:Consultation = Consultation.fromDCObject(resource)
                        logger.debug("Binding $routingKey received consultation ${consultation.objet}")
                        marcheSecuriseService.updateConsultation(login, password, pa, consultation, resource.getUri(), updateConsultationUrl)
                    }

                    routingBindingKeyOfType(routingKey,"marchepublic:lot_0") -> {
                        val lot: Lot = Lot.toLot(resource)
                        logger.debug("Binding $routingKey received lot ${lot.libelle}")
                        marcheSecuriseService.updateLot(login, password, pa, lot, resource.getUri(), lotUrl)
                    }

                    else -> logger.warn("Unable to recognize type (consultation, lot or piece) from routing key $routingKey")
                }

            routingBindingKeyOfAction(routingKey, BindingKeyAction.DELETE) ->
                    when {
                        routingBindingKeyOfType(routingKey,"marchepublic:consultation_0") -> {
                            logger.debug("Binding $routingKey received deletion order for consultation ${resource.getUri()}")
                            marcheSecuriseService.deleteConsultation(login, password, pa, resource.getUri(), deleteConsultationUrl)
                        }

                        routingBindingKeyOfType(routingKey, "marchepublic:lot_0") -> {
                            logger.debug("Binding $routingKey received deletion order for lot ${resource.getUri()}")
                            marcheSecuriseService.deleteLot(login, password, pa, resource.getUri(), lotUrl)
                        }

                        routingBindingKeyOfType(routingKey, "marchepublic:piece_0") -> {
                            logger.debug("Binding $routingKey received deletion order for piece ${resource.getUri()}")
                            marcheSecuriseService.deletePiece(login, password, pa, resource.getUri(), pieceUrl)
                        }

                        else -> logger.warn("Unable to recognize type (consultation, lot or piece) from routing key $routingKey")
                    }

            routingBindingKeyOfAction(routingKey, BindingKeyAction.PUBLISH) ->
                    when{
                        routingBindingKeyOfType(routingKey, "marchepublic:consultation_0") -> {
                            logger.debug("Binding $routingKey received publication order for consultation ${resource.getUri()}")
                            marcheSecuriseService.publishConsultation(login, password, pa, resource.getUri(), publishConsultationUrl)
                        }
                        else -> logger.warn("Unable to recognize type (consultation, lot or piece) from routing key $routingKey")
                    }

            else -> logger.warn("Unable to recognize action (create, update, delete) from routing key $routingKey")
        }
    }

    fun routingBindingKeyOfAction(routingKey: String, controlKey : BindingKeyAction): Boolean {
        return routingKey.substringAfterLast('.') == controlKey.value
    }

    fun routingBindingKeyOfType(routingKey: String, type: String): Boolean {
        return routingKey.split('.')[2] == type
    }
}