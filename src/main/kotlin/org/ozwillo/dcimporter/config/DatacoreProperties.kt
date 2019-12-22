package org.ozwillo.dcimporter.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties("datacore")
data class DatacoreProperties(
    val containerUrl: String,
    val typePrefix: String,
    val url: String,
    val systemAdminUser: SystemAdminUser
) {
    data class SystemAdminUser(
        val refreshToken: String,
        val nonce: String
    )

    fun baseResourceUri() = "$containerUrl/$typePrefix"
}
