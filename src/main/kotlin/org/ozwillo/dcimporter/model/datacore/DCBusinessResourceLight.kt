package org.ozwillo.dcimporter.model.datacore

import com.fasterxml.jackson.annotation.JsonAnyGetter
import com.fasterxml.jackson.annotation.JsonAnySetter
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
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
}