package org.ozwillo.dcimporter.model.sirene

import com.fasterxml.jackson.annotation.JsonProperty

class Response{
    @JsonProperty(value = "etablissement")
    val etablissement: Etablissement? = null
    @JsonProperty(value = "header")
    val header: SireneHeader? = null
}