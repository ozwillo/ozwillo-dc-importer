package org.ozwillo.dcimporter.config

import org.oasis_eu.spring.kernel.security.StaticOpenIdCConfiguration

class OpenIdConnectConfiguration : StaticOpenIdCConfiguration() {

    override fun requireAuthenticationForPath(path: String): Boolean {
        return !path.contains("/api") && !path.contains("/error")
    }
}
