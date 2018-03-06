package org.ozwillo.dcimporter.model.datacore

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
open class DCResourceLight(private val uri: String) {

    @JsonProperty("@id")
    fun getUri(): String {
        return uri
    }

    @JsonIgnore
    fun getIri(): String {
        val modelTypeIndex = uri.indexOf(DCResource.dcTypeMidfix) + DCResource.dcTypeMidfix.length
        val idSlashIndex = uri.indexOf('/', modelTypeIndex)

        return uri.substring(idSlashIndex + 1)
    }
}