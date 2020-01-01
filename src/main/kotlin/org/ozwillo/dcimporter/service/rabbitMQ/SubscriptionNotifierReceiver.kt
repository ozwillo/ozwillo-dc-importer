package org.ozwillo.dcimporter.service.rabbitMQ

import com.rabbitmq.client.Channel
import org.ozwillo.dcimporter.config.ApplicationProperties
import org.ozwillo.dcimporter.service.SubscriptionService
import org.springframework.amqp.core.*
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.amqp.support.AmqpHeaders
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.messaging.handler.annotation.Header
import org.springframework.stereotype.Service

@Service
class SubscriptionNotifierReceiver(
    private val applicationProperties: ApplicationProperties,
    private val subscriptionService: SubscriptionService
) {

    companion object {
        const val QUEUE_NAME = "subscription_queue"
    }

    @Bean(name = [QUEUE_NAME])
    fun subscriptionQueue(): Queue {
        return Queue(QUEUE_NAME)
    }

    @Bean
    fun subscriptionBinding(topic: TopicExchange, @Qualifier(QUEUE_NAME) queue: Queue): Binding {
        return BindingBuilder.bind(queue)
            .to(TopicExchange(applicationProperties.amqp.exchangerName))
            .with("#")
    }

    @RabbitListener(queues = [ProcessingStatReceiver.QUEUE_NAME])
    fun receive(incoming: Message, channel: Channel, @Header(AmqpHeaders.DELIVERY_TAG) tag: Long) {
        val routingKey = incoming.messageProperties.receivedRoutingKey
        subscriptionService.notifyForEventType(routingKey, String(incoming.body))
    }
}
