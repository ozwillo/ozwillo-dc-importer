package org.ozwillo.dcimporter.model.publik

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class Submission(
    var channel: String,
    var backoffice: Boolean
)
