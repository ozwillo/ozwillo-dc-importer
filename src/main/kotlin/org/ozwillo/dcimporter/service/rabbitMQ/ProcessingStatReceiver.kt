package org.ozwillo.dcimporter.service.rabbitMQ

import com.rabbitmq.client.Channel
import org.ozwillo.dcimporter.config.ApplicationProperties
import org.ozwillo.dcimporter.model.ProcessingStat
import org.ozwillo.dcimporter.service.ProcessingStatService
import org.slf4j.LoggerFactory
import org.springframework.amqp.core.*
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.amqp.support.AmqpHeaders
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.messaging.handler.annotation.Header
import org.springframework.stereotype.Service

@Service
class ProcessingStatReceiver(
    private val processingStatService: ProcessingStatService,
    private val applicationProperties: ApplicationProperties
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    companion object {
        const val QUEUE_NAME = "stat_queue"
    }

    @Bean(name = [QUEUE_NAME])
    fun statQueue(): Queue {
        return Queue(QUEUE_NAME)
    }

    @Bean
    fun statBinding(topic: TopicExchange, @Qualifier(QUEUE_NAME) queue: Queue): Binding {
        return BindingBuilder.bind(queue).to(TopicExchange(applicationProperties.amqp.exchangerName)).with("#")
    }

    @RabbitListener(queues = [QUEUE_NAME])
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
