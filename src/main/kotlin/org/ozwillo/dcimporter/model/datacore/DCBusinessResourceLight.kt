package org.ozwillo.dcimporter.model.datacore

import com.fasterxml.jackson.annotation.JsonAnyGetter
import com.fasterxml.jackson.annotation.JsonAnySetter
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.time.LocalDateTime
import java.util.HashMap

@JsonIgnoreProperties("@type")
class DCBusinessResourceLight(uri: String,
                              @JsonAnySetter private var values: Map<String, String> = HashMap()) : DCResourceLight(uri) {

    @JsonAnyGetter
    fun getValues(): Map<String, String> {
        return values
    }

    fun setStringValue(key: String, value: String) {
        this.values = this.values.plus(Pair(key, value))
    }

    fun setDateTimeValue(key: String, value: LocalDateTime) {
        this.values = this.values.plus(Pair(key, value.toString()))
    }

    fun setListValue(key: String, values: List<Any>) {
        this.values = this.values.plus(Pair(key, values.toString()))
    }

    @JsonIgnore
    private val resourceFiles: MutableList<DCBusinessResourceFile> = mutableListOf()

    fun addResourceFile(dcBusinessResourceFile: DCBusinessResourceFile) {
        this.resourceFiles.add(dcBusinessResourceFile)
    }

    fun gimmeResourceFile(): DCBusinessResourceFile = resourceFiles[0]
}

data class DCBusinessResourceFile(
        val base64content: String,
        val contentType: String,
        val filename: String)