package org.ozwillo.dcimporter.model.publik

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class ListFormsModel(
    var id: Int,
    var url: String,
    var last_update_time: String,
    var receipt_time: String)
