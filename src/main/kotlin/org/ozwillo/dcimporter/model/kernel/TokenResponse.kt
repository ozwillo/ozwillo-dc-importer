package org.ozwillo.dcimporter.model.kernel

import com.fasterxml.jackson.annotation.JsonProperty

class TokenResponse {

    @JsonProperty("access_token")
    var accessToken: String? = null
    @JsonProperty("expires_in")
    var expiresIn: Int = 0
    @JsonProperty("id_token")
    var idToken: String? = null
    @JsonProperty("scope")
    var scope: String? = null
    @JsonProperty("token_type")
    var tokenType: String? = null

    @JsonProperty(value = "refresh_token", required = false)
    var refreshToken: String? = null

    override fun toString(): String {
        return "TokenResponse(accessToken=$accessToken, expiresIn=$expiresIn, idToken=$idToken, scope=$scope, tokenType=$tokenType, refreshToken=$refreshToken)"
    }
}
