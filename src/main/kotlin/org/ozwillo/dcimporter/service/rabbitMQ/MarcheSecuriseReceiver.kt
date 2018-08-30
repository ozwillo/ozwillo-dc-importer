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

    @RabbitListener(queues = ["deadletter"])
    @Throws(InterruptedException::class)
    fun dealFailedMessage(failedMessage: Message, channel: Channel, @Header(AmqpHeaders.DELIVERY_TAG)tag: Long){
        val retriesHeader:Int = if(failedMessage.messageProperties.headers["x-retries"] == null) 0 else (failedMessage.messageProperties.headers["x-retries"] as Int)
        val delayFactor = if(failedMessage.messageProperties.headers["x-delay-factor"] == null) 1 else (failedMessage.messageProperties.headers["x-delay-factor"] as Int)
        val messageId = failedMessage.messageProperties.headers["message-id"]

        when{
            retriesHeader < 50 -> {
                failedMessage.messageProperties.headers["x-retries"] = retriesHeader+1
                failedMessage.messageProperties.headers["x-delay-factor"] = delayFactor * 2
                failedMessage.messageProperties.delay = delayFactor * 1900000   // delay 1/2 hour -> *2 each try for 50 tries
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
                        val response = marcheSecuriseService.createAndUpdateConsultation(routingBindingKeySiret(routingKey), consultation, resource.getUri())

                        when{
                            MSUtils.checkResponse(response, consultation.reference.toString()) -> {
                                if (response.contains("statut=\"not_changed\""))
                                    logger.warn("An error occurs in consultation data saving, please check consultation and update with correct data if needed")
                                channel.basicAck(tag, false)
                                logger.debug("Creation of consultation ${consultation.reference} successful")
                            }
                            response.contains("ref ${consultation.reference} already exist") -> {
                                channel.basicAck(tag, false)
                                logger.warn("Unable to create consultation in Marchés Sécurisés because resource with ref ${consultation.reference} already exists.")
                            }
                            !MSUtils.errorReturn(response).isEmpty() -> {
                                logger.warn(MSUtils.errorReturn(response))
                                channel.basicReject(tag, false)
                            }
                            else -> channel.basicReject(tag, true)
                        }
                    }

                    routingBindingKeyOfType(routingKey, "marchepublic:lot_0") -> {
                        val lot: Lot = Lot.toLot(resource)
                        logger.debug("Binding $routingKey received lot ${lot.libelle}")
                        val response = marcheSecuriseService.createLot(routingBindingKeySiret(routingKey), lot, resource.getUri())
                        when{
                            MSUtils.checkResponse(response, lot.ordre.toString()) -> {
                                channel.basicAck(tag, false)
                                logger.debug("Creation of lot ${lot.libelle} successful")
                            }
                            !MSUtils.errorReturn(response).isEmpty() -> {
                                logger.warn(MSUtils.errorReturn(response))
                                channel.basicReject(tag, false)
                            }
                            else -> channel.basicReject(tag, true)
                        }
                    }

                    routingBindingKeyOfType(routingKey, "marchepublic:piece_0") -> {
                        val piece:Piece = Piece.toPiece(resource)
                        logger.debug("Binding $routingKey received piece ${piece.libelle}")
                        val response = marcheSecuriseService.createPiece(routingBindingKeySiret(routingKey), piece, resource.getUri())
                        when{
                            MSUtils.checkResponse(response, "${piece.nom}.${piece.extension}") -> {
                                channel.basicAck(tag, false)
                                logger.debug("Creation of piece ${piece.libelle} successful")
                            }
                            response.contains(SoapPieceResponse.CREATION_FAILED_FILE_ALREADY_EXIST.value) -> {
                                channel.basicAck(tag, false)
                                logger.warn("Unable to create piece ${piece.libelle} in Marchés Sécurisés because a file with the name ${piece.nom}.${piece.extension} already exist. Please delete and retry with a new Name")
                            }
                            !MSUtils.errorReturn(response).isEmpty() -> {
                                logger.warn(MSUtils.errorReturn(response))
                                channel.basicReject(tag, false)
                            }
                            else -> channel.basicReject(tag, true)
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
                        val response = marcheSecuriseService.updateConsultation(routingBindingKeySiret(routingKey), consultation, resource.getUri())
                        when {
                            MSUtils.checkResponse(response, consultation.reference.toString()) -> {
                                if (response.contains("statut=\"not_changed\""))
                                    logger.warn("An error occurs in consultation data saving, please check consultation and update with correct data if needed")
                                channel.basicAck(tag, false)
                                logger.debug("Update of consultation ${consultation.reference} successful")
                            }
                            !MSUtils.errorReturn(response).isEmpty() -> {
                                logger.warn(MSUtils.errorReturn(response))
                                channel.basicReject(tag, false)
                            }
                            else -> channel.basicReject(tag, true)
                        }
                    }

                    routingBindingKeyOfType(routingKey,"marchepublic:lot_0") -> {
                        val lot: Lot = Lot.toLot(resource)
                        logger.debug("Binding $routingKey received lot ${lot.libelle}")
                        val response = marcheSecuriseService.updateLot(routingBindingKeySiret(routingKey), lot, resource.getUri())
                        when{
                            MSUtils.checkResponse(response, lot.ordre.toString())-> {
                                channel.basicAck(tag, false)
                                logger.debug("Update of lot ${lot.libelle} successful")
                            }
                            !MSUtils.errorReturn(response).isEmpty() -> {
                                logger.warn(MSUtils.errorReturn(response))
                                channel.basicReject(tag, false)
                            }
                            else -> channel.basicReject(tag, true)
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
                            val response = marcheSecuriseService.deleteConsultation(routingBindingKeySiret(routingKey), resource.getUri())
                            when {
                                MSUtils.checkResponse(response, resource.getUri()) -> {
                                    channel.basicAck(tag, false)
                                    logger.debug("Delete successful")
                                }
                                !MSUtils.errorReturn(response).isEmpty() -> {
                                    logger.warn(MSUtils.errorReturn(response))
                                    channel.basicReject(tag, false)
                                }
                                else -> channel.basicReject(tag, true)
                            }
                        }

                        routingBindingKeyOfType(routingKey, "marchepublic:lot_0") -> {
                            logger.debug("Binding $routingKey received deletion order for lot ${resource.getUri()}")
                            val response = marcheSecuriseService.deleteLot(routingBindingKeySiret(routingKey), resource.getUri())
                            when {
                                MSUtils.checkResponse(response, resource.getUri()) -> {
                                    channel.basicAck(tag, false)
                                    logger.debug("Delete successful")
                                }
                                !MSUtils.errorReturn(response).isEmpty() -> {
                                    logger.warn(MSUtils.errorReturn(response))
                                    channel.basicReject(tag, false)
                                }
                                else -> channel.basicReject(tag, true)
                            }
                        }

                        routingBindingKeyOfType(routingKey, "marchepublic:piece_0") -> {
                            logger.debug("Binding $routingKey received deletion order for piece ${resource.getUri()}")
                            val response = marcheSecuriseService.deletePiece(routingBindingKeySiret(routingKey), resource.getUri())
                            when {
                                MSUtils.checkResponse(response, resource.getUri()) -> {
                                    channel.basicAck(tag, false)
                                    logger.debug("Delete successful")
                                }
                                !MSUtils.errorReturn(response).isEmpty() -> {
                                    logger.warn(MSUtils.errorReturn(response))
                                    channel.basicReject(tag, false)
                                }
                                else -> channel.basicReject(tag, true)
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
                            val response = marcheSecuriseService.publishConsultation(routingBindingKeySiret(routingKey), resource.getUri())
                            when{
                                MSUtils.checkResponse(response, resource.getUri()) -> {
                                    channel.basicAck(tag, false)
                                    logger.debug("Publication successful")
                                }
                                response.contains(SoapConsultationResponse.PUBLICATION_REJECTED.value) -> {
                                    channel.basicAck(tag, false)
                                    logger.warn("Publication rejected. Please update with correct data and retry")
                                }
                                !MSUtils.errorReturn(response).isEmpty() -> {
                                    logger.warn(MSUtils.errorReturn(response))
                                    channel.basicReject(tag, false)
                                }
                                else -> channel.basicReject(tag, true)
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