package org.ozwillo.dcimporter.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties("application")
data class ApplicationProperties(
    val url: String,
    val amqp: AmqpConfig,
    val iot: IotConfig
) {
    data class AmqpConfig(
        val defaultExchangerName: String,
        val exchangerName: String
    )

    data class IotConfig(
        val bindingKey: String
    )
}
