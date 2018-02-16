package org.ozwillo.dcimporter.model.publik

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class User(
    var email: String,
    @JsonProperty("NameID")
    var nameID: ArrayList<String>,
    var id: Int,
    var name: String)
