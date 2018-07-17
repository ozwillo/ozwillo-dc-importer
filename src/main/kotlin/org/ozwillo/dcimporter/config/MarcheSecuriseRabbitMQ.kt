package org.ozwillo.dcimporter.config

import org.ozwillo.dcimporter.service.MarcheSecuriseService
import org.ozwillo.dcimporter.service.rabbitMQ.Receiver
import org.ozwillo.dcimporter.service.rabbitMQ.Sender
import org.ozwillo.dcimporter.util.JsonConverter
import org.springframework.amqp.core.Binding
import org.springframework.amqp.core.BindingBuilder
import org.springframework.amqp.core.Queue
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.amqp.core.TopicExchange
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value

@Configuration
class MarcheSecuriseRabbitMQ {

    @Value("\${marchesecurise.config.amqp.exchangerName}")
    private val EXCHANGER_NAME = ""


    @Bean
    fun topic(): TopicExchange {
        return TopicExchange(EXCHANGER_NAME)
    }

    private class ReceiverConfig {

        @Autowired
        private lateinit var marcheSecuriseService: MarcheSecuriseService

        @Value("\${marchesecurise.config.amqp.queueName}")
        private val QUEUE_MS_NAME = ""
        @Value("\${marchesecurise.config.amqp.bindingKey}")
        private val BINDING_KEY = ""

        @Bean
        fun receiver(): Receiver {
            return Receiver(marcheSecuriseService)
        }

        @Bean
        fun queueMS(): Queue {
            return Queue(QUEUE_MS_NAME)
        }

        @Bean
        fun bindingConsultation(topic: TopicExchange, queueMS: Queue): Binding {
            return BindingBuilder.bind(queueMS).to(topic).with(BINDING_KEY)
        }

    }

    @Bean
    fun sender(): Sender {
        return Sender()
    }

}