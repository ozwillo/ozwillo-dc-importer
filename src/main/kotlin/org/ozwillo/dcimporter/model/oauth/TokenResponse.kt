package org.ozwillo.dcimporter.model.oauth

import com.fasterxml.jackson.annotation.JsonProperty

data class TokenResponse(
    @JsonProperty("access_token")
    val accessToken: String,
    @JsonProperty("expires_in")
    val expiresIn: Long,
    @JsonProperty("id_token")
    var idToken: String? = null,
    val scope: String,
    @JsonProperty("token_type")
    val tokenType: String
)
