package org.ozwillo.dcimporter.config.marchesecurise

import org.ozwillo.dcimporter.service.marchesecurise.rabbitMQ.ReceiverMS
import org.ozwillo.dcimporter.service.marchesecurise.rabbitMQ.SenderMS
import org.springframework.amqp.core.Binding
import org.springframework.amqp.core.BindingBuilder
import org.springframework.amqp.core.Queue
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.amqp.core.TopicExchange

@Configuration
class MarcheSecuriseRabbitMQ {
    private val EXCHANGER_NAME = "dcimporter"

    @Bean
    fun topic(): TopicExchange {
        return TopicExchange(EXCHANGER_NAME)
    }

    private class ReceiverConfig {

        private val QUEUE_MS_NAME = "marchesecurise"
        private val KEY_CONSULTATION = "consultation.#"

        @Bean
        fun receiver(): ReceiverMS {
            return ReceiverMS()
        }

        @Bean
        fun queueMS(): Queue {
            return Queue(QUEUE_MS_NAME)
        }

        @Bean
        fun bindingConsultation(topic: TopicExchange, queueMS: Queue): Binding {
            return BindingBuilder.bind(queueMS).to(topic).with(KEY_CONSULTATION)
        }

    }

    @Bean
    fun sender(): SenderMS {
        return SenderMS()
    }

}