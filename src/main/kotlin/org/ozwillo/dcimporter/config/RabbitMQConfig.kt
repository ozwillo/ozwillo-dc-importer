package org.ozwillo.dcimporter.config

import org.springframework.amqp.core.Binding
import org.springframework.amqp.core.BindingBuilder
import org.springframework.amqp.core.Queue
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.amqp.core.TopicExchange
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value

@Configuration
class RabbitMQConfig {

    @Value("\${amqp.config.exchangerName}")
    private val EXCHANGER_NAME = ""


    @Bean
    fun topic(): TopicExchange {
        return TopicExchange(EXCHANGER_NAME)
    }

    private class MarcheSecuriseReceiverConfig {

        @Value("\${amqp.config.marchesecurise.queueName}")
        private val QUEUE_MS_NAME = ""
        @Value("\${amqp.config.marchesecurise.bindingKey}")
        private val BINDING_KEY = ""

        @Bean(name = ["queue_ms"])
        fun queueMS(): Queue {
            return Queue(QUEUE_MS_NAME)
        }

        @Bean
        fun bindingToMarcheSecurise(topic: TopicExchange, @Qualifier("queue_ms") queueMS: Queue): Binding {
            return BindingBuilder.bind(queueMS).to(topic).with(BINDING_KEY)
        }

    }

    private class MaarchReceiverConfig {

        @Value("\${amqp.config.maarch.queueName}")
        private val QUEUE_MAARCH_NAME = ""
        @Value("\${amqp.config.maarch.bindingKey}")
        private val BINDING_KEY = ""

        @Bean(name = ["queue_maarch"])
        fun queueMaarch(): Queue {
            return Queue(QUEUE_MAARCH_NAME)
        }

        @Bean
        fun bindingToMaarch(topic: TopicExchange, @Qualifier("queue_maarch") queue: Queue): Binding {
            return BindingBuilder.bind(queue).to(topic).with(BINDING_KEY)
        }
    }

    private class PublikReceiverConfig {

        @Value("\${amqp.config.publik.queueName}")
        private val QUEUE_PUBLIK_NAME = ""
        @Value("\${amqp.config.publik.bindingKey}")
        private val BINDING_KEY = ""

        @Bean(name = ["queue_publik"])
        fun queuePublik(): Queue {
            return Queue(QUEUE_PUBLIK_NAME)
        }

        @Bean
        fun bindingToPublik(topic: TopicExchange, @Qualifier("queue_publik") queue: Queue): Binding {
            return BindingBuilder.bind(queue).to(topic).with(BINDING_KEY)
        }
    }
}