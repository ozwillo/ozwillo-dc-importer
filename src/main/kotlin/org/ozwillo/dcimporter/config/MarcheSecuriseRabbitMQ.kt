package org.ozwillo.dcimporter.config

import org.ozwillo.dcimporter.repository.BusinessMappingRepository
import org.ozwillo.dcimporter.service.marchesecurise.MarcheSecuriseService
import org.ozwillo.dcimporter.service.marchesecurise.rabbitMQ.ReceiverMS
import org.ozwillo.dcimporter.service.marchesecurise.rabbitMQ.SenderMS
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
        private lateinit var businessMappingRepository: BusinessMappingRepository

        @Autowired
        private lateinit var marcheSecuriseService: MarcheSecuriseService

        @Value("\${marchesecurise.config.amqp.queueName}")
        private val QUEUE_MS_NAME = ""
        @Value("\${marchesecurise.config.amqp.bindingKey}")
        private val BINDING_KEY = ""

        @Bean
        fun receiver(): ReceiverMS {
            return ReceiverMS(businessMappingRepository, marcheSecuriseService)
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
    fun sender(): SenderMS {
        return SenderMS()
    }

}