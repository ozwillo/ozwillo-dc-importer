package org.ozwillo.dcimporter.model.sirene

import com.fasterxml.jackson.annotation.JsonProperty

class Adresse{
    @JsonProperty(value = "codePostalEtablissement")
    val cp: String? = null
    @JsonProperty(value = "libelleVoieEtablissement", required = false)
    val libelleVoie: String? = null
    @JsonProperty(value = "numeroVoieEtablissement", required = false)
    val numero: String? = null
    @JsonProperty(value = "typeVoieEtablissement", required = false)
    val typeVoie: String? = null
}