package org.ozwillo.dcimporter.model.datacore

import com.fasterxml.jackson.annotation.JsonAnyGetter
import com.fasterxml.jackson.annotation.JsonAnySetter
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

@JsonIgnoreProperties("@type")
class DCBusinessResourceLight(uri: String,
                              @JsonAnySetter private var values: Map<String, Any> = HashMap()) : DCResourceLight(uri) {

    @JsonIgnore
    val df = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault())

    @JsonAnyGetter
    fun getValues(): Map<String, Any> {
        return values
    }

    fun setStringValue(key: String, value: String) {
        this.values = this.values.plus(Pair(key, value))
    }

    fun setBooleanValue(key: String, value: Boolean) {
        this.values = this.values.plus(Pair(key, value))
    }

    fun setIntegerValue(key: String, value: Int) {
        this.values = this.values.plus(Pair(key, value))
    }

    fun setDateTimeValue(key: String, value: LocalDateTime) {
        this.values = this.values.plus(Pair(key, value.format(df)))
    }

    fun setListValue(key: String, values: List<Any>) {
        this.values = this.values.plus(Pair(key, values))
    }

    @JsonIgnore
    private val resourceFiles: MutableList<DCBusinessResourceFile> = mutableListOf()

    fun addResourceFile(dcBusinessResourceFile: DCBusinessResourceFile) {
        this.resourceFiles.add(dcBusinessResourceFile)
    }

    fun gimmeResourceFile(): DCBusinessResourceFile = resourceFiles[0]

    fun getStringValue(s: String): String = values[s]?.let { it as String }.orEmpty()

    fun getIntValue(s: String): Int = values[s] as Int

    fun getBooleanValue(s: String): Boolean = values[s] as Boolean

    fun getDateValue(s: String): LocalDateTime = LocalDateTime.parse(values[s] as String, df)

    fun getIntListValue(s: String): List<Int> = values[s] as List<Int>

    fun getStringListValue(s: String): List<String> = values[s] as List<String>
}

data class DCBusinessResourceFile(
        val base64content: String,
        val contentType: String,
        val filename: String)