package org.ozwillo.dcimporter.service.rabbitMQ

import com.rabbitmq.client.Channel
import org.ozwillo.dcimporter.model.ProcessingStat
import org.ozwillo.dcimporter.service.ProcessingStatService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.amqp.core.Message
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.amqp.support.AmqpHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.stereotype.Service

@Service
class ProcessingStatReceiver(private val processingStatService: ProcessingStatService) {

    private val logger: Logger = LoggerFactory.getLogger(ProcessingStatReceiver::class.java)

    @RabbitListener(queues = ["stat"])
    @Throws(InterruptedException::class)
    fun receive(incoming: Message, channel: Channel, @Header(AmqpHeaders.DELIVERY_TAG) tag: Long) {

        // TODO find why we can do with this organization thing (it's not an universal concept shared by all
        //      the data that goes throw the application
        if (incoming.messageProperties.headers.containsKey("original-routing-key")) {
            val routingKey: String = incoming.messageProperties.headers["original-routing-key"].toString()
            val messageId = incoming.messageProperties.headers["message-id"].toString()

            logger.debug("Received message with routing key : $routingKey")

            processingStatService.create(
                ProcessingStat(
                    id = messageId,
                    model = routingKey.split(".")[1],
                    organization = null,
                    action = routingKey.split(".")[2]
                )
            )
        } else {
            logger.info("Ignoring message with no routing key")
        }

        channel.basicAck(tag, false)
    }
}
