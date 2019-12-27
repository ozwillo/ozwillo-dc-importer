package org.ozwillo.dcimporter.config

import org.springframework.amqp.core.TopicExchange
import org.springframework.amqp.rabbit.annotation.EnableRabbit
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableRabbit
class RabbitMQConfig(
    private val applicationProperties: ApplicationProperties
) {

    @Bean
    fun topic(): TopicExchange {
        return TopicExchange(applicationProperties.amqp.exchangerName)
    }
}
