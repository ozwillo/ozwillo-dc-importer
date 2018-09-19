package org.ozwillo.dcimporter.model.sirene

import com.fasterxml.jackson.annotation.JsonProperty

class UniteLegale{
    @JsonProperty(value = "nomUniteLegale", required = false)
    val nomUniteLegale: String? = null
    @JsonProperty(value = "denominationUniteLegale", required = false)
    val denominationUniteLegale: String? = null
}