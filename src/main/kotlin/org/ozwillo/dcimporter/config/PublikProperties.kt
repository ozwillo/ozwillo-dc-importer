package org.ozwillo.dcimporter.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties("publik")
data class PublikProperties(
    val formTypeEM: String,
    val formTypeSVE: String,
    val algo: String,
    val orig: String,
    val bindingKey: String
)
