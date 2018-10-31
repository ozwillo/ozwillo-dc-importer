package org.ozwillo.dcimporter.config

import com.rabbitmq.client.Channel
import org.mockito.ArgumentMatchers.anyBoolean
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.willReturn
import org.mockito.Mockito.mock
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory
import org.springframework.amqp.rabbit.connection.Connection
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter
import org.springframework.amqp.rabbit.test.TestRabbitTemplate
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RabbitMockConfig {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(RabbitMockConfig::class.java)
    }

    @Bean
    fun template(): TestRabbitTemplate {
        return TestRabbitTemplate(connectionFactory())
    }

    @Bean
    fun connectionFactory(): ConnectionFactory {
        val factory: ConnectionFactory = mock(ConnectionFactory::class.java)
        val connection: Connection = mock(Connection::class.java)
        val channel: Channel = mock(Channel::class.java)
        willReturn(connection).given(factory).createConnection()
        willReturn(channel).given(connection).createChannel(anyBoolean())
        given(channel.isOpen).willReturn(true)
        return factory
    }

    @Bean
    fun rabbitListenerContainerFactory(): SimpleRabbitListenerContainerFactory {
        val factory = SimpleRabbitListenerContainerFactory()
        factory.setConnectionFactory(connectionFactory())
        return factory
    }

    @Bean
    fun smlc(): SimpleMessageListenerContainer {
        val container = SimpleMessageListenerContainer(connectionFactory())
        // FIXME : make this more dynamic
        // maybe extend TestRabbitTemplate to able to wildcard queue names ?
        container.setQueueNames(
            "marchepublic_0.123456789.marchepublic:consultation_0.create",
            "marchepublic_0.123456789.marchepublic:consultation_0.update",
            "marchepublic_0.123456789.marchepublic:consultation_0.delete",
            "marchepublic_0.123456789.marchepublic:consultation_0.publish",
            "marchepublic_0.123456789.marchepublic:lot_0.create",
            "marchepublic_0.123456789.marchepublic:lot_0.update",
            "marchepublic_0.123456789.marchepublic:lot_0.delete",
            ".123456789.grant:association_0.create",
            ".987654321.grant:association_0.create",
            ".987654321.orgfr:Organisation_0.create",
            ".123456789.grant:association_0.update",
            ".987654321.grant:association_0.update",
            "marchepublic_0.123456789.marchepublic:reponse_0.create",
            "marchepublic_0.123456789.marchepublic:reponse_0.update",
            "marchepublic_0.123456789.marchepublic:retrait_0.create",
            "marchepublic_0.123456789.marchepublic:retrait_0.update",
            "marchepublic_0.987654321.orgfr:Organisation_0.create",
            "marchepublic_0.1335945366ODE0aP0Xgd.marchepublic:personne_0.create",
            "marchepublic_0.1335945366ODE0aP0Xgd.marchepublic:personne_0.update"
        )
        container.setMessageListener(MessageListenerAdapter(object {
            fun handleMessage(message: Any) {
                LOGGER.debug("Got message $message")
            }
        }))
        return container
    }
}