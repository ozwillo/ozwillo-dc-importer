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

    private class MarcheSecuriseReceiverConfig {

        @Value("\${amqp.config.marchesecurise.queueName}")
        private val QUEUE_MS_NAME = ""
        @Value("\${amqp.config.marchesecurise.deadLetterQueue}")
        private val DEAD_LETTER_QUEUE_NAME = ""
        @Value("\${amqp.config.marchesecurise.bindingKey}")
        private val BINDING_KEY = ""

        @Bean(name = ["queue_ms"])
        fun queueMS(): Queue {
            return QueueBuilder.durable(QUEUE_MS_NAME)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", DEAD_LETTER_QUEUE_NAME)
                .build()
        }

        @Bean(name = ["queue_deadletter"])
        fun deadLetterQueue(): Queue {
            return Queue(DEAD_LETTER_QUEUE_NAME)
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

    private class EGMReceiverConfig {

        @Value("\${amqp.config.egm.queueName}")
        private val QUEUE_EGM_NAME = ""
        @Value("\${amqp.config.egm.bindingKey}")
        private val BINDING_KEY = ""

        @Bean(name = ["queue_egm"])
        fun queueEGM(): Queue {
            return Queue(QUEUE_EGM_NAME)
        }


        @Bean
        fun bindingToEGM(topic: TopicExchange, @Qualifier("queue_egm") queue: Queue): Binding {
            // TODO : make topic name a config parameter
            return BindingBuilder.bind(queue).to(TopicExchange("amq.topic")).with(BINDING_KEY)
        }

//        @Bean
//        fun binding1a(topic: TopicExchange, @Qualifier("queue_egm") queue: Queue
//        ): Binding {
//            return BindingBuilder.bind(queue)
//                .to(topic)
//                .with(BINDING_KEY)
//        }
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