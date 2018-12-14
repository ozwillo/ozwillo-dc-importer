package org.ozwillo.dcimporter.service.rabbitMQ

import com.rabbitmq.client.Channel
import org.ozwillo.dcimporter.model.ProcessingStat
import org.ozwillo.dcimporter.service.ProcessingStatService
import org.springframework.amqp.core.Message
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.amqp.support.AmqpHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.stereotype.Service

@Service
class ProcessingStatReceiver (private val processingStatService: ProcessingStatService){

    @RabbitListener(queues = ["stat"])
    @Throws(InterruptedException::class)
    fun receive(incoming: Message, channel: Channel, @Header(AmqpHeaders.DELIVERY_TAG) tag: Long){
        val routingKey: String = incoming.messageProperties.headers["original-routing-key"].toString()
        val messageId = incoming.messageProperties.headers["message-id"].toString()

        processingStatService.create(
            ProcessingStat(
                id = messageId,
                model = routingKey.split(".")[2],
                organization = routingKey.split(".")[1],
                action = routingKey.split(".")[3]
            )
        )
        channel.basicAck(tag, false)
    }
}