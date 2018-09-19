package org.ozwillo.dcimporter.model.sirene

import com.fasterxml.jackson.annotation.JsonProperty

class SireneHeader{
    @JsonProperty(value = "message")
    val message: String? = null
    @JsonProperty(value = "statut")
    val status: Int? = null
}