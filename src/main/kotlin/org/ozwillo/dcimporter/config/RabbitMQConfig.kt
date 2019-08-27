package org.ozwillo.dcimporter.config

import org.springframework.amqp.core.*
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.amqp.core.TopicExchange
import org.springframework.amqp.core.BindingBuilder

@Configuration
class RabbitMQConfig {

    @Value("\${amqp.config.exchangerName}")
    private val EXCHANGER_NAME = ""

    @Bean
    fun topic(): TopicExchange {
        return TopicExchange(EXCHANGER_NAME)
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

    private class IoTReceiverConfig {

        @Value("\${amqp.config.iot.queueName}")
        private val QUEUE_IOT_NAME = ""
        @Value("\${amqp.config.iot.bindingKey}")
        private val BINDING_KEY = ""

        @Bean(name = ["queue_iot"])
        fun queueEGM(): Queue {
            return Queue(QUEUE_IOT_NAME)
        }

        @Bean
        fun bindingToIoT(topic: TopicExchange, @Qualifier("queue_iot") queue: Queue): Binding {
            // TODO : make topic name a config parameter
            return BindingBuilder.bind(queue).to(TopicExchange("amq.topic")).with(BINDING_KEY)
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

    private class ProcessingStatReceiverConfig {
        @Value("\${amqp.config.processingStat.queueName}")
        private val QUEUE_PROCESSING_STAT_NAME = ""
        @Value("\${amqp.config.processingStat.bindingKey}")
        private val BINDING_KEY = ""

        @Bean(name = ["queue_processing_stat"])
        fun queueProcessingStat(): Queue {
            return Queue(QUEUE_PROCESSING_STAT_NAME)
        }

        @Bean
        fun bindingToProcessingStat(topic: TopicExchange, @Qualifier("queue_processing_stat") queue: Queue): Binding {
            return BindingBuilder.bind(queue).to(topic).with(BINDING_KEY)
        }
    }
}
