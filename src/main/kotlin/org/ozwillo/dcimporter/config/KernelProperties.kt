package org.ozwillo.dcimporter.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties("kernel")
class KernelProperties {
    lateinit var accountUri: String
    lateinit var clientId: String
    lateinit var clientSecret: String
}