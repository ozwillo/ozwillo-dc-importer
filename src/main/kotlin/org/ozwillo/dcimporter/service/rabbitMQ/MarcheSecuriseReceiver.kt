package org.ozwillo.dcimporter.service.rabbitMQ

import com.rabbitmq.client.Channel
import org.ozwillo.dcimporter.model.marchepublic.Consultation
import org.ozwillo.dcimporter.model.marchepublic.Lot
import org.ozwillo.dcimporter.model.marchepublic.Piece
import org.ozwillo.dcimporter.service.MarcheSecuriseService
import org.ozwillo.dcimporter.util.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.amqp.core.Message
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.amqp.support.AmqpHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.stereotype.Service

@Service
class MarcheSecuriseReceiver (val marcheSecuriseService: MarcheSecuriseService, private val template: RabbitTemplate) {

    private val logger:Logger = LoggerFactory.getLogger(MarcheSecuriseReceiver::class.java)

    @RabbitListener(queues = ["marchesecurise"])
    @Throws(InterruptedException::class)
    fun receive(incoming: Message, channel: Channel, @Header(AmqpHeaders.DELIVERY_TAG)tag: Long) {
        routingByBindingKey(incoming, channel, tag)
    }

    @RabbitListener(queues = ["ms.deadletter"])
    @Throws(InterruptedException::class)
    fun dealFailedMessage(failedMessage: Message, channel: Channel, @Header(AmqpHeaders.DELIVERY_TAG)tag: Long){
        val retriesHeader:Int = if(failedMessage.messageProperties.headers["x-retries"] == null) 0 else (failedMessage.messageProperties.headers["x-retries"] as Int)
        val delayFactor = if(failedMessage.messageProperties.headers["x-delay-factor"] == null) 1 else (failedMessage.messageProperties.headers["x-delay-factor"] as Int)
        val messageId = failedMessage.messageProperties.headers["message-id"]

        when{
            retriesHeader < 50 -> {
                failedMessage.messageProperties.headers["x-retries"] = retriesHeader+1
                failedMessage.messageProperties.headers["x-delay-factor"] = delayFactor * 2
                failedMessage.messageProperties.delay = delayFactor * 900000   // delay 1/4 hour -> *2 each try for 50 tries
                val routingKey = failedMessage.messageProperties.headers["original-routing-key"].toString()
                this.template.convertAndSend("dcimporter", routingKey, failedMessage)
                logger.debug("Failed message ${failedMessage.body} with routing key $routingKey about to be re-send to marchesecurise in ${failedMessage.messageProperties.delay} ms (${failedMessage.messageProperties.delay/3600000} hours)")
                channel.basicAck(tag, false)
            }
            else -> logger.warn("Unable to finalize message treatment. Please check Dead Letter queue for message with id $messageId.")
        }
    }

    fun routingByBindingKey(incoming: Message, channel: Channel, tag: Long) {
        val message = String(incoming.body)
        val routingKey: String = incoming.messageProperties.headers["original-routing-key"].toString()
        val resource = JsonConverter.jsonToObject(message)

        when {
            routingBindingKeyOfAction(routingKey, BindingKeyAction.CREATE) ->
                when {
                    routingBindingKeyOfType(routingKey,"marchepublic:consultation_0") -> {
                        val consultation:Consultation = Consultation.fromDCObject(resource)
                        logger.debug("Binding $routingKey received consultation ${consultation.objet}")
                        try {
                            val responseObject = marcheSecuriseService.createAndUpdateConsultation(routingBindingKeySiret(routingKey), consultation, resource.getUri())
                            when{
                                responseObject.properties != null && responseObject.properties!!.size >= 8 && responseObject.properties!![8].value == consultation.reference && responseObject.properties!![8].status == "changed" -> {
                                    for (i in responseObject.properties!!.indices){
                                        if (responseObject.properties!![i].message == "value_not_allowed"){
                                            logger.warn("An error occurs in consultation creation, value not allowed  for ${responseObject.properties!![i].name}. Please update it.")
                                        }
                                    }
                                    channel.basicAck(tag, false)
                                    logger.debug("Creation of consultation ${consultation.reference} successful")
                                }
                                responseObject.type == "error" && responseObject.properties != null && responseObject.properties!![0].name == "load_pa_error" -> {
                                    logger.warn("Unable to process to consultation creation in Marchés Sécurisés because of following error : Bad Pa")
                                    channel.basicReject(tag, false)
                                }
                                else -> channel.basicReject(tag, true)
                            }
                        }catch (e: ConsultationDuplicateError){
                            channel.basicReject(tag, false)
                            logger.warn(e.message)
                        }catch (e: BadLogError){
                            channel.basicReject(tag, false)
                            logger.warn(e.message)
                        }catch (e: SoapParsingUnexpectedError){
                            channel.basicReject(tag, false)
                            logger.warn(e.message)
                        }
                    }

                    routingBindingKeyOfType(routingKey, "marchepublic:lot_0") -> {
                        val lot: Lot = Lot.toLot(resource)
                        logger.debug("Binding $routingKey received lot ${lot.libelle}")
                        try {
                            val responseObject = marcheSecuriseService.createLot(routingBindingKeySiret(routingKey), lot, resource.getUri())
                            when{
                                responseObject.properties != null && responseObject.properties!!.find { p -> p.value == "error" } == null && responseObject.properties!!.size >= 6 && responseObject.properties!!.find { p -> p.value.toString() == lot.ordre.toString() } != null -> {
                                    channel.basicAck(tag, false)
                                    logger.debug("Creation of lot ${lot.libelle} successful")
                                }
                                responseObject.properties != null && responseObject.properties!![0].name == "load_dce_error" -> {
                                    logger.warn("Unable to process to lot creation in Marchés Sécurisés because of following error : Bad Dce")
                                    channel.basicReject(tag, false)
                                }
                                responseObject.properties != null && responseObject.properties!![0].name == "load_pa_error" -> {
                                    logger.warn("Unable to process to lot creation in Marchés Sécurisés because of following error : Bad Pa")
                                    channel.basicReject(tag, false)
                                }
                                else -> channel.basicReject(tag, true)
                            }
                        }catch (e: LotDuplicateError){
                            channel.basicReject(tag, false)
                            logger.warn(e.message)
                        }catch (e: BadLogError){
                            channel.basicReject(tag, false)
                            logger.warn(e.message)
                        }catch (e: SoapParsingUnexpectedError){
                            channel.basicReject(tag, false)
                            logger.warn(e.message)
                        }
                    }

                    routingBindingKeyOfType(routingKey, "marchepublic:piece_0") -> {
                        val piece:Piece = Piece.toPiece(resource)
                        logger.debug("Binding $routingKey received piece ${piece.libelle}")
                        try {
                            val responseObject = marcheSecuriseService.createPiece(routingBindingKeySiret(routingKey), piece, resource.getUri())
                            when{
                                responseObject.type != "error" && responseObject.properties != null && responseObject.properties!!.size >= 5 && responseObject.properties!![5].value == "${piece.nom}.${piece.extension}" -> {
                                    channel.basicAck(tag, false)
                                    logger.debug("Creation of piece ${piece.libelle} successful")
                                }
                                responseObject.type == "error" && responseObject.properties!![0].value == " file_exist" -> {
                                    channel.basicAck(tag, false)
                                    logger.warn("Unable to create piece ${piece.libelle} in Marchés Sécurisés because a file with the name ${piece.nom}.${piece.extension} already exist. Please delete piece uri ${resource.getUri()} and retry with a new Name")
                                }
                                else -> channel.basicReject(tag, true)
                            }
                        }catch (e: PieceSizeError){
                            channel.basicAck(tag, false)
                            logger.warn(e.message)
                        }catch (e: BadDceError){
                            channel.basicReject(tag, false)
                            logger.warn(e.message)
                        }catch (e: BadPaError){
                            channel.basicReject(tag, false)
                            logger.warn(e.message)
                        }catch (e: BadLogError){
                            channel.basicReject(tag, false)
                            logger.warn(e.message)
                        }catch (e: SoapParsingUnexpectedError){
                            channel.basicReject(tag, false)
                            logger.warn(e.message)
                        }
                    }
                    else -> {
                        logger.warn("Unable to recognize type (consultation, lot or piece) from routing key $routingKey")
                        channel.basicReject(tag, false)}
                }

            routingBindingKeyOfAction(routingKey, BindingKeyAction.UPDATE) ->
                when {
                    routingBindingKeyOfType(routingKey,"marchepublic:consultation_0") -> {
                        val consultation:Consultation = Consultation.fromDCObject(resource)
                        logger.debug("Binding $routingKey received consultation ${consultation.objet}")
                        try {
                            val responseObject = marcheSecuriseService.updateConsultation(routingBindingKeySiret(routingKey), consultation, resource.getUri())
                            when {
                                responseObject.properties != null && responseObject.properties!!.size >= 8 && responseObject.properties!![8].value == consultation.reference -> {
                                    for (i in responseObject.properties!!.indices){
                                        if (responseObject.properties!![i].message == "value_not_allowed"){
                                            logger.warn("An error occurs in consultation updating, value not allowed  for ${responseObject.properties!![i].name}. Please update it.")
                                        }
                                    }
                                    channel.basicAck(tag, false)
                                    logger.debug("Update of consultation ${consultation.reference} successful")
                                }
                                responseObject.type == "error" && responseObject.properties != null && responseObject.properties!![0].name == "load_pa_error" -> {
                                    logger.warn("Unable to process to consultation updating in Marchés Sécurisés beacause of following error : Bad Pa")
                                    channel.basicReject(tag, false)
                                }
                                responseObject.properties != null && responseObject.properties!![0].name == "load_consultation_fail" && responseObject.properties!![0].message == "no_consultation" -> {
                                    logger.warn("Unable to process to consultation updating in Marchés Sécurisés beacause of following error : Bad Dce")
                                    channel.basicReject(tag, false)
                                }
                                responseObject.properties != null && responseObject.properties!![0].name == "array_expected" && responseObject.properties!![0].message == "no_array" -> {
                                    logger.warn("Unable to process to consultation updating in Marchés Sécurisés beacause of following error : Bad array format. Please check request format")
                                    channel.basicReject(tag, false)
                                }
                                else -> channel.basicReject(tag, true)
                            }
                        }catch (e: BadLogError){
                            channel.basicReject(tag, false)
                            logger.warn(e.message)
                        }catch (e: SoapParsingUnexpectedError){
                            channel.basicReject(tag, false)
                            logger.warn(e.message)
                        }
                    }

                    routingBindingKeyOfType(routingKey,"marchepublic:lot_0") -> {
                        val lot: Lot = Lot.toLot(resource)
                        logger.debug("Binding $routingKey received lot ${lot.libelle}")
                        try {
                            val responseObject = marcheSecuriseService.updateLot(routingBindingKeySiret(routingKey), lot, resource.getUri())
                            when{
                                responseObject.properties != null && responseObject.properties!!.find { p -> p.value == "error" } == null && responseObject.properties!!.find { p -> p.value.toString() == lot.ordre.toString() } != null -> {
                                    channel.basicAck(tag, false)
                                    logger.debug("Update of lot ${lot.libelle} successful")
                                }
                                responseObject.properties != null && responseObject.properties!!.size >= 6 && responseObject.properties!![6].name == "load_lot_error" -> {
                                    logger.warn("Unable to process to lot updating in Marchés Sécurisés because of following error : Bad cleLot")
                                    channel.basicReject(tag, false)
                                }
                                responseObject.properties != null && responseObject.properties!![0].name == "load_dce_error" -> {
                                    logger.warn("Unable to process to lot updating in Marchés Sécurisés because of following error : Bad Dce")
                                    channel.basicReject(tag, false)
                                }
                                responseObject.properties != null && responseObject.properties!![0].name == "load_pa_error" -> {
                                    logger.warn("Unable to process to lot updating in Marchés Sécurisés because of following error : Bad Pa")
                                    channel.basicReject(tag, false)
                                }
                                else -> channel.basicReject(tag, true)
                            }
                        }catch (e: BadLogError){
                            channel.basicReject(tag, false)
                            logger.warn(e.message)
                        }catch (e: SoapParsingUnexpectedError){
                            channel.basicReject(tag, false)
                            logger.warn(e.message)
                        }
                    }
                    else -> {
                        logger.warn("Unable to recognize type (consultation, lot or piece) from routing key $routingKey")
                        channel.basicReject(tag, false)
                    }
                }

            routingBindingKeyOfAction(routingKey, BindingKeyAction.DELETE) ->
                    when {
                        routingBindingKeyOfType(routingKey,"marchepublic:consultation_0") -> {
                            logger.debug("Binding $routingKey received deletion order for consultation ${resource.getUri()}")
                            try {
                                val responseObject = marcheSecuriseService.deleteConsultation(routingBindingKeySiret(routingKey), resource.getUri())
                                when {
                                    responseObject.consultationState == "supprimee" -> {
                                        channel.basicAck(tag, false)
                                        logger.debug("Delete successful")
                                    }
                                    else -> channel.basicReject(tag, true)
                                }
                            }catch (e: BadDceError){
                                channel.basicReject(tag, false)
                                logger.warn(e.message)
                            }catch (e: BadPaError){
                                channel.basicReject(tag, false)
                                logger.warn(e.message)
                            }catch (e: BadLogError){
                                channel.basicReject(tag, false)
                                logger.warn(e.message)
                            }catch (e: SoapParsingUnexpectedError){
                                channel.basicReject(tag, false)
                                logger.warn(e.message)
                            }
                        }

                        routingBindingKeyOfType(routingKey, "marchepublic:lot_0") -> {
                            logger.debug("Binding $routingKey received deletion order for lot ${resource.getUri()}")
                            try {
                                val responseObject = marcheSecuriseService.deleteLot(routingBindingKeySiret(routingKey), resource.getUri())
                                when {
                                    responseObject.properties != null && responseObject.properties!!.find { p -> p.value == "error" } == null && (responseObject.properties!!.size >= 5 || responseObject.properties!![0].value == "supprime") -> {
                                        channel.basicAck(tag, false)
                                        logger.debug("Delete successful")
                                    }
                                    responseObject.properties != null && responseObject.properties!![0].name == "load_lot_error" -> {
                                        logger.warn("Unable to process to lot creation in Marchés Sécurisés because of following error : Bad cleLot")
                                        channel.basicReject(tag, false)
                                    }
                                    responseObject.properties != null && responseObject.properties!![0].name == "load_dce_error" -> {
                                        logger.warn("Unable to process to lot creation in Marchés Sécurisés because of following error : Bad Dce")
                                        channel.basicReject(tag, false)
                                    }
                                    responseObject.properties != null && responseObject.properties!![0].name == "load_pa_error" -> {
                                        logger.warn("Unable to process to lot creation in Marchés Sécurisés because of following error : Bad Pa")
                                        channel.basicReject(tag, false)
                                    }
                                    else -> channel.basicReject(tag, true)
                                }
                            }catch (e: BadLogError){
                                channel.basicReject(tag, false)
                                logger.warn(e.message)
                            }catch (e: DeletionError){
                                channel.basicReject(tag, false)
                                logger.warn(e.message)
                            }catch (e: SoapParsingUnexpectedError){
                                channel.basicReject(tag, false)
                                logger.warn(e.message)
                            }
                        }

                        routingBindingKeyOfType(routingKey, "marchepublic:piece_0") -> {
                            logger.debug("Binding $routingKey received deletion order for piece ${resource.getUri()}")
                            try {
                                val responseObject = marcheSecuriseService.deletePiece(routingBindingKeySiret(routingKey), resource.getUri())
                                when {
                                    responseObject.properties != null && (responseObject.properties!![0].name == "cle_piece" || responseObject.properties!![0].value == "supprime") -> {
                                        channel.basicAck(tag, false)
                                        logger.debug("Delete successful")
                                    }
                                    else -> channel.basicReject(tag, true)
                                }
                            }catch (e: BadClePiece){
                                channel.basicReject(tag, false)
                                logger.warn(e.message)
                            }catch (e: BadDceError){
                                channel.basicReject(tag, false)
                                logger.warn(e.message)
                            }catch (e: BadPaError){
                                channel.basicReject(tag, false)
                                logger.warn(e.message)
                            }catch (e: BadLogError){
                                channel.basicReject(tag, false)
                                logger.warn(e.message)
                            }catch (e: SoapParsingUnexpectedError){
                                channel.basicReject(tag, false)
                                logger.warn(e.message)
                            }
                        }

                        else -> {
                            logger.warn("Unable to recognize type (consultation, lot or piece) from routing key $routingKey")
                            channel.basicReject(tag, false)
                        }
                    }

            routingBindingKeyOfAction(routingKey, BindingKeyAction.PUBLISH) ->
                    when{
                        routingBindingKeyOfType(routingKey, "marchepublic:consultation_0") -> {
                            logger.debug("Binding $routingKey received publication order for consultation ${resource.getUri()}")
                            try {
                                val responseObject = marcheSecuriseService.publishConsultation(routingBindingKeySiret(routingKey), resource.getUri())
                                when{
                                    responseObject.properties != null && responseObject.properties!!.size == 20 -> {
                                        channel.basicAck(tag, false)
                                        logger.debug("Publication successful")
                                    }
                                    else -> channel.basicReject(tag, true)
                                }
                            }catch (e: CheckConsultationRejectedError){
                                channel.basicAck(tag, false)
                                logger.warn(e.message)
                            }catch (e: PublishConsultationRejectedError){
                                channel.basicAck(tag, false)
                                logger.warn(e.message)
                            }catch (e: BadDceError){
                                channel.basicReject(tag, false)
                                logger.warn(e.message)
                            }catch (e: BadPaError){
                                channel.basicReject(tag, false)
                                logger.warn(e.message)
                            }catch (e: BadLogError){
                                channel.basicReject(tag, false)
                                logger.warn(e.message)
                            }catch (e: SoapParsingUnexpectedError){
                                channel.basicReject(tag, false)
                                logger.warn(e.message)
                            }
                        }
                        else -> {
                            logger.warn("Unable to recognize type (consultation, lot or piece) from routing key $routingKey")
                            channel.basicReject(tag, false)
                        }
                    }

            else -> {
                logger.warn("Unable to recognize action (create, update, delete) from routing key $routingKey")
                channel.basicReject(tag, false)
            }
        }
    }

    fun routingBindingKeyOfAction(routingKey: String, controlKey : BindingKeyAction): Boolean {
        return routingKey.substringAfterLast('.') == controlKey.value
    }

    fun routingBindingKeyOfType(routingKey: String, type: String): Boolean {
        return routingKey.split('.')[2] == type
    }

    fun routingBindingKeySiret(routingKey: String): String = routingKey.split('.')[1]
}