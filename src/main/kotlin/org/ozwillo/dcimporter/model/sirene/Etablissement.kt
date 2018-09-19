package org.ozwillo.dcimporter.model.sirene

import com.fasterxml.jackson.annotation.JsonProperty

class Etablissement{
    @JsonProperty(value = "adresseEtablissement")
    val adresse: Adresse? = null
    @JsonProperty(value = "siret")
    val siret: String? = null
    @JsonProperty(value = "uniteLegale")
    val uniteLegale: UniteLegale? = null
}