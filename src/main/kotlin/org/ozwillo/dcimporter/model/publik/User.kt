package org.ozwillo.dcimporter.model.publik

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class User(
    val email: String,
    @JsonProperty("NameID")
    val nameID: ArrayList<String>,
    val id: Int,
    val name: String)
