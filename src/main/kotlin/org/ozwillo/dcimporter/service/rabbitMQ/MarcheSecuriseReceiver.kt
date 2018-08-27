package org.ozwillo.dcimporter.service.rabbitMQ

import com.rabbitmq.client.Channel
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
import org.springframework.amqp.support.AmqpHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.stereotype.Service

@Service
class MarcheSecuriseReceiver (val marcheSecuriseService: MarcheSecuriseService) {

    private val logger:Logger = LoggerFactory.getLogger(MarcheSecuriseReceiver::class.java)

    @RabbitListener(queues = ["marchesecurise"])
    @Throws(InterruptedException::class)
    fun receive(incoming: Message, channel: Channel, @Header(AmqpHeaders.DELIVERY_TAG)tag: Long) {
        val message = String(incoming.body)
        val routingKey = incoming.messageProperties.receivedRoutingKey
        val resource = JsonConverter.jsonToObject(message)

        routingByBindingKey(resource, routingKey, channel, tag)
    }

    fun routingByBindingKey(resource: DCBusinessResourceLight, routingKey: String, channel: Channel, tag: Long) {
        when {
            routingBindingKeyOfAction(routingKey, BindingKeyAction.CREATE) ->
                when {
                    routingBindingKeyOfType(routingKey,"marchepublic:consultation_0") -> {
                        val consultation:Consultation = Consultation.fromDCObject(resource)
                        logger.debug("Binding $routingKey received consultation ${consultation.objet}")
                        val response = marcheSecuriseService.createAndUpdateConsultation(routingBindingKeySiret(routingKey), consultation, resource.getUri())
                        when{
                            response.contains("<propriete nom=\"ref_interne\" statut=\"changed\">") -> {
                                channel.basicReject(tag, false)
                                logger.debug("Creation of consultation ${consultation.reference} successful")
                            }
                            response.contains("<propriete nom=\"load_pa_error\">error</propriete>") -> {
                                channel.basicReject(tag, false)
                                logger.debug("Unable to create consultation in Marchés Sécurisés because of incorrect pa. Please delete and retry with correct connectors.")
                            }
                            response.contains("<return xsi:nil=\"true\"/>") -> {
                                channel.basicReject(tag, false)
                                logger.debug("Unable to create consultation in Marchés Sécurisés because of unknown login/password. Please delete and retry with correct connectors.")
                            }
                            else -> channel.basicReject(tag, true)
                        }
                    }

                    routingBindingKeyOfType(routingKey, "marchepublic:lot_0") -> {
                        val lot: Lot = Lot.toLot(resource)
                        logger.debug("Binding $routingKey received lot ${lot.libelle}")
                        val response = marcheSecuriseService.createLot(routingBindingKeySiret(routingKey), lot, resource.getUri())
                        when{
                            response.contains("<propriete nom=\"ordre\">${lot.ordre}</propriete>") -> {
                                channel.basicReject(tag, false)
                                logger.debug("Creation of lot ${lot.libelle} successful")
                            }
                            response.contains("<propriete nom=\"load_dce_error\">error</propriete>") -> {
                                channel.basicReject(tag, false)
                                logger.warn("Unable to create lot in Marchés Sécurisés because the specified consultation is not found. Please delete and retry with correct consultation reference or dce (check businessMapping).")
                            }
                            response.contains("<propriete nom=\"load_pa_error\">error</propriete>") -> {
                                channel.basicReject(tag, false)
                                logger.debug("Unable to create lot in Marchés Sécurisés because of incorrect pa. Please delete and retry with correct connectors.")
                            }
                            response.contains("<return xsi:nil=\"true\"/>") -> {
                                channel.basicReject(tag, false)
                                logger.debug("Unable to create lot in Marchés Sécurisés because of unknown login/password. Please delete and retry with correct connectors.")
                            }
                            else -> channel.basicReject(tag, true)
                        }
                    }

                    routingBindingKeyOfType(routingKey, "marchepublic:piece_0") -> {
                        val piece:Piece = Piece.toPiece(resource)
                        logger.debug("Binding $routingKey received piece ${piece.libelle}")
                        val response = marcheSecuriseService.createPiece(routingBindingKeySiret(routingKey), piece, resource.getUri())
                        when{
                            response.contains("<propriete nom=\"nom\">${piece.nom}.${piece.extension}</propriete>") -> {
                                channel.basicReject(tag, false)
                                logger.debug("Creation of piece ${piece.libelle} successful")
                            }
                            response.contains("<propriete nom=\"fichier_error\"> file_exist</propriete>") -> {
                                channel.basicReject(tag, false)
                                logger.debug("Unable to create piece ${piece.libelle} in Marchés Sécurisés because a file with the name ${piece.nom}.${piece.extension} already exist")
                            }
                            response.contains("<consultation_non_trouvee") -> {
                                channel.basicReject(tag, false)
                                logger.warn("Unable to create piece ${piece.libelle} in Marchés Sécurisés because the specified consultation is not found. Please delete and retry with correct consultation reference or dce (check businessMapping).")
                            }
                            response.contains("<pa_non_trouvee") -> {
                                channel.basicReject(tag, false)
                                logger.debug("Unable to create piece ${piece.libelle} in Marchés Sécurisés because of incorrect pa. Please delete and retry with correct connectors.")
                            }
                            response.contains("<logs_non_trouves") -> {
                                channel.basicReject(tag, false)
                                logger.debug("Unable to create lot in Marchés Sécurisés because of unknown login/password. Please delete and retry with correct connectors.")
                            }
                            else -> channel.basicReject(tag, true)
                        }
                    }

                    else -> logger.warn("Unable to recognize type (consultation, lot or piece) from routing key $routingKey")
                }

            routingBindingKeyOfAction(routingKey, BindingKeyAction.UPDATE) ->
                when {
                    routingBindingKeyOfType(routingKey,"marchepublic:consultation_0") -> {
                        val consultation:Consultation = Consultation.fromDCObject(resource)
                        logger.debug("Binding $routingKey received consultation ${consultation.objet}")
                        val response = marcheSecuriseService.updateConsultation(routingBindingKeySiret(routingKey), consultation, resource.getUri())
                        when {
                            response.contains("<propriete nom=\"cle\">") -> {
                                channel.basicReject(tag, false)
                                logger.debug("Update of consultation ${consultation.reference} successful")
                            }
                            response.contains("<propriete nom=\"load_consultation_fail\" statut=\"not_changed\" message=\"no_consultation\">no_consultation</propriete>") -> {
                                channel.basicReject(tag, false)
                                logger.warn("Unable to update consultation ${consultation.reference} in Marchés Sécurisés because the specified object is not found. Please retry with correct consultation reference or dce (check businessMapping).")
                            }
                            response.contains("<propriete nom=\"load_pa_error\">error</propriete>") -> {
                                channel.basicReject(tag, false)
                                logger.debug("Unable to update consultation in Marchés Sécurisés because of incorrect pa. Please retry with correct connectors.")
                            }
                            response.contains("<return xsi:nil=\"true\"/>") -> {
                                channel.basicReject(tag, false)
                                logger.debug("Unable to update consultation in Marchés Sécurisés because of unknown login/password. Please retry with correct connectors.")
                            }
                            else -> channel.basicReject(tag, true)
                        }
                    }

                    routingBindingKeyOfType(routingKey,"marchepublic:lot_0") -> {
                        val lot: Lot = Lot.toLot(resource)
                        logger.debug("Binding $routingKey received lot ${lot.libelle}")
                        val response = marcheSecuriseService.updateLot(routingBindingKeySiret(routingKey), lot, resource.getUri())
                        when{
                            (response.contains("<propriete nom=\"ordre\">${lot.ordre}</propriete>|<propriete nom=\"ordre\" statut=\"changed\">${lot.ordre}</propriete>".toRegex()) && !response.contains("<propriete nom=\"load_lot_error\">error</propriete>"))-> {
                                channel.basicReject(tag, false)
                                logger.debug("Update of lot ${lot.libelle} successful")
                            }
                            response.contains("<propriete nom=\"load_lot_error\">error</propriete>") -> {
                                channel.basicReject(tag, false)
                                logger.warn("Unable to update lot in Marchés Sécurisés because the specified object is not found. Please retry with correct lot uuid or cleLot (check businessMapping).")
                            }
                            response.contains("<propriete nom=\"load_dce_error\">error</propriete>") -> {
                                channel.basicReject(tag, false)
                                logger.warn("Unable to update lot in Marchés Sécurisés because the specified consultation is not found. Please retry with correct consultation reference or dce (check businessMapping).")
                            }
                            response.contains("<propriete nom=\"load_pa_error\">error</propriete>") -> {
                                channel.basicReject(tag, false)
                                logger.debug("Unable to update lot in Marchés Sécurisés because of incorrect pa. Please retry with correct connectors.")
                            }
                            response.contains("<return xsi:nil=\"true\"/>") -> {
                                channel.basicReject(tag, false)
                                logger.debug("Unable to update lot in Marchés Sécurisés because of unknown login/password. Please retry with correct connectors.")
                            }
                            else -> channel.basicReject(tag, true)
                        }
                    }

                    else -> logger.warn("Unable to recognize type (consultation, lot or piece) from routing key $routingKey")
                }

            routingBindingKeyOfAction(routingKey, BindingKeyAction.DELETE) ->
                    when {
                        routingBindingKeyOfType(routingKey,"marchepublic:consultation_0") -> {
                            logger.debug("Binding $routingKey received deletion order for consultation ${resource.getUri()}")
                            val response = marcheSecuriseService.deleteConsultation(routingBindingKeySiret(routingKey), resource.getUri())
                            when {
                                response.contains("<consultation_suppr_ok etat_consultation=\"supprimee\"/>") -> {
                                    channel.basicReject(tag, false)
                                    logger.debug("Delete successful")
                                }
                                response.contains("<consultation_cle_error") -> {
                                    channel.basicReject(tag, false)
                                    logger.warn("Unable to delete consultation from Marchés Sécurisés because the specified object is not found. Please retry with correct lot uuid or cleLot (check businessMapping).")
                                }
                                response.contains("<pa_suppr_dce_error".toRegex()) -> {
                                    channel.basicReject(tag, false)
                                    logger.debug("Unable to delete consultation from Marchés Sécurisés because of incorrect pa. Please retry with correct connectors.")
                                }
                                response.contains("<log_error") -> {
                                    channel.basicReject(tag, false)
                                    logger.debug("Unable to delete consultation from Marchés Sécurisés because of unknown login/password. Please retry with correct connectors.")
                                }
                                else -> channel.basicReject(tag, true)
                            }
                        }

                        routingBindingKeyOfType(routingKey, "marchepublic:lot_0") -> {
                            logger.debug("Binding $routingKey received deletion order for lot ${resource.getUri()}")
                            val response = marcheSecuriseService.deleteLot(routingBindingKeySiret(routingKey), resource.getUri())
                            when {
                                (response.contains("<propriete suppression=\"true\">supprime</propriete>|<objet type=\"ms_v2__fullweb_lot\">".toRegex()) && !response.contains(("<propriete nom=\"load_lot_error\">error</propriete>"))) -> {
                                    channel.basicReject(tag, false)
                                    logger.debug("Delete successful")
                                }
                                response.contains("<propriete nom=\"load_lot_error\">error</propriete>") -> {
                                    channel.basicReject(tag, false)
                                    logger.warn("Unable to delete lot from Marchés Sécurisés because the specified object is not found. Please retry with correct lot uuid or cleLot (check businessMapping).")
                                }
                                response.contains("<propriete nom=\"load_dce_error\">error</propriete>") -> {
                                    channel.basicReject(tag, false)
                                    logger.warn("Unable to delete lot from Marchés Sécurisés because the specified consultation is not found. Please retry with correct consultation reference or dce (check businessMapping).")
                                }
                                response.contains("<propriete nom=\"load_pa_error\">error</propriete>") -> {
                                    channel.basicReject(tag, false)
                                    logger.debug("Unable to delete lot from Marchés Sécurisés because of incorrect pa. Please retry with correct connectors.")
                                }
                                response.contains("<return xsi:nil=\"true\"/>") -> {
                                    channel.basicReject(tag, false)
                                    logger.debug("Unable to delete lot from Marchés Sécurisés because of unknown login/password. Please retry with correct connectors.")
                                }
                                else -> channel.basicReject(tag, true)
                            }
                        }

                        routingBindingKeyOfType(routingKey, "marchepublic:piece_0") -> {
                            logger.debug("Binding $routingKey received deletion order for piece ${resource.getUri()}")
                            val response = marcheSecuriseService.deletePiece(routingBindingKeySiret(routingKey), resource.getUri())
                            when{
                                response.contains("<objet type=\"ms_v2__fullweb_piece\">") -> {
                                    channel.basicReject(tag, false)
                                    logger.debug("Delete successful")
                                }
                                response.contains("<cle_piece_non_trouvee") -> {
                                    channel.basicReject(tag, false)
                                    logger.warn("Unable to delete piece from Marchés Sécurisés because the specified object is not found. Please retry with correct piece uuid or clePiece (check businessMapping).")
                                }
                                response.contains("<cle_dce_non_trouvee") -> {
                                    channel.basicReject(tag, false)
                                    logger.warn("Unable to delete piece from Marchés Sécurisés because the specified consultation is not found. Please retry with correct consultation reference or dce (check businessMapping).")
                                }
                                response.contains("<pa_non_trouvee") -> {
                                    channel.basicReject(tag, false)
                                    logger.debug("Unable to delete piece from Marchés Sécurisés because of incorrect pa. Please retry with correct connectors.")
                                }
                                response.contains("<logs_non_trouves".toRegex()) -> {
                                    channel.basicReject(tag, false)
                                    logger.debug("Unable to delete piece from Marchés Sécurisés because of unknown login/password. Please retry with correct connectors.")
                                }
                                else -> channel.basicReject(tag, true)
                            }
                        }

                        else -> logger.warn("Unable to recognize type (consultation, lot or piece) from routing key $routingKey")
                    }

            routingBindingKeyOfAction(routingKey, BindingKeyAction.PUBLISH) ->
                    when{
                        routingBindingKeyOfType(routingKey, "marchepublic:consultation_0") -> {
                            logger.debug("Binding $routingKey received publication order for consultation ${resource.getUri()}")
                            val response = marcheSecuriseService.publishConsultation(routingBindingKeySiret(routingKey), resource.getUri())
                            when{
                                response.contains("<propriete nom=\"cle\">") -> {
                                    channel.basicReject(tag, false)
                                    logger.debug("Publication successful")
                                }
                                response.contains("<validation_erreur") -> {
                                    channel.basicReject(tag, false)
                                    logger.debug("Publication rejected. Please retry with correct datas.")
                                }
                                response.contains("<dce_error".toRegex()) -> {
                                    channel.basicReject(tag, false)
                                    logger.debug("Unable to publish consultation in Marchés Sécurisés because the specified object is not found. Please retry with correct consultation reference or dce (check businessMapping).")
                                }
                                response.contains("<pa_error".toRegex()) -> {
                                    channel.basicReject(tag, false)
                                    logger.debug("Unable to publish consultation in Marchés Sécurisés because of incorrect pa. Please retry with correct connectors.")
                                }
                                response.contains("<identification_error".toRegex()) -> {
                                    channel.basicReject(tag, false)
                                    logger.debug("Unable to publish consultation in Marchés Sécurisés because of unknown login/password. Please retry with correct connectors.")
                                }
                                else -> channel.basicReject(tag, true)
                            }
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

    fun routingBindingKeySiret(routingKey: String): String = routingKey.split('.')[1]
}