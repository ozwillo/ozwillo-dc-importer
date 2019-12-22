package org.ozwillo.dcimporter.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties("insee.api.sirene")
data class InseeSireneProperties(
    val baseUri: String,
    val tokenPath: String,
    val siretPath: String,
    val siretParameters: String,
    val secretClient: String
)
