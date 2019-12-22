package org.ozwillo.dcimporter.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties("kernel")
data class KernelProperties(
    val clientId: String,
    val clientSecret: String,
    val tokenEndpoint: String
)
