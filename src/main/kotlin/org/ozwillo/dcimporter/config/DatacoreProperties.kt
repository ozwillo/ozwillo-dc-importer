package org.ozwillo.dcimporter.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties("datacore")
class DatacoreProperties {
    lateinit var containerUrl: String
    lateinit var typePrefix: String
    lateinit var baseUri: String
    lateinit var url: String
    var systemAdminUser: SystemAdminUser = SystemAdminUser()

    class SystemAdminUser {
        lateinit var refreshToken: String
        lateinit var nonce: String
    }
}
