package org.ozwillo.dcimporter.model.datacore

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import java.io.UnsupportedEncodingException
import java.net.URLDecoder
import java.net.URLEncoder
import java.time.Instant
import java.util.*
import java.util.stream.Collectors
import kotlin.streams.toList

@JsonIgnoreProperties(ignoreUnknown = true)
class DCResource(
        @JsonProperty("@id") val id: String,
        @JsonProperty("o:version") val version: Int = INITIAL_VERSION,
        val type: String) {

    companion object {
        /** used to split id in order to encode its path elements if it's not disabled */
        private const val URL_SAFE_CHARACTERS_BESIDES_ALPHANUMERIC = "\\$\\-_\\.\\+!\\*'\\(\\)"
        private const val URL_ALSO_SAFE_PATH_SEGMENT_OR_SLASH_CHARACTERS = ":@~&,;=/"
        private const val URL_SAFE_PATH_SEGMENT_OR_SLASH_CHARACTERS_BESIDES_ALPHANUMERIC = URL_SAFE_CHARACTERS_BESIDES_ALPHANUMERIC + URL_ALSO_SAFE_PATH_SEGMENT_OR_SLASH_CHARACTERS

        const val INITIAL_VERSION = -1

        const val dcTypeMidfix = "/dc/type/"

        fun encodeUriPathSegment(uriPathSegment: String): String {
            val sb = StringBuilder()
            try {
                for (c in uriPathSegment.toCharArray()) {
                    if ( (c.toInt() in 48..57) // number
                            || (c.toInt() in 65..90) // upper case
                            || (c.toInt() in 97..122) // lower case
                            || URL_SAFE_PATH_SEGMENT_OR_SLASH_CHARACTERS_BESIDES_ALPHANUMERIC.indexOf(c) != -1) { // among safe chars
                        sb.append(c)
                    } else {
                        sb.append(URLEncoder.encode(String(Character.toChars(c.toInt())) , "UTF-8"))
                    }
                }
            } catch (e: UnsupportedEncodingException) {
                // should never happens for UTF-8
                throw RuntimeException(e)
            }
            return sb.toString()
        }

        fun guessTypeFromUri(uri: String): String {
            val modelTypeIndex = uri.indexOf(dcTypeMidfix) + dcTypeMidfix.length
            val idSlashIndex = uri.indexOf('/', modelTypeIndex)
            val encodedType = uri.substring(modelTypeIndex, idSlashIndex)
            try {
                return URLDecoder.decode(encodedType, "UTF-8")
            } catch (e: UnsupportedEncodingException) {
                // should never happens for UTF-8
                throw RuntimeException(e)
            }

        }
    }

    /** (encoded) */
    var baseUri: String? = null
    /** (encoded) */
    var iri: String? = null

    /** cache */
    var encodedType: String = encodeUriPathSegment(this.type!!)

    var created: Instant? = null
    var lastModified: Instant? = null
    var createdBy: String? = null
    var lastModifiedBy: String? = null

    var values: Map<String, Value> = HashMap()

    /** cache (encoded) */
    fun getUri(): String = "$baseUri/$encodedType/$iri"

    /**
     * Can also be used to parse an uri by creating a dummy Resource and setting its uri
     * @param uri
     */
    fun setUriComponents(uri: String) {
        val modelTypeIndex = uri.indexOf(dcTypeMidfix) + dcTypeMidfix.length
        val idSlashIndex = uri.indexOf('/', modelTypeIndex)

        this.iri = uri.substring(idSlashIndex+1) // (encoded)
        this.baseUri = uri.substring(0, modelTypeIndex-1) // (encoded)
    }

    fun isNew(): Boolean {
        return version == -1
    }

    // Convenience methods follow

    fun setStringValue(key: String, value: String) {
        this.values = this.values.plus(Pair(key, StringValue(value)))
    }

    fun setMappedListValue(key: String, values: List<Map<String,Value>>) {
        val arrayValue: List<MapValue> = values.stream().map {
            MapValue(it)
        }.toList()

        this.values = this.values.plus(Pair(key, ArrayValue(arrayValue)))
    }

    fun setListValue(key: String, values: List<String>) {
        val arrayValue: List<StringValue> = values.stream().map {
            StringValue(it)
        }.toList()

        this.values = this.values.plus(Pair(key, ArrayValue(arrayValue)))
    }

    fun getAsString(key: String): String? {
        val value: Value? = this.values[key]
        if (value == null) return null
        if (value.isArray()) {
            throw IllegalArgumentException("Value for $key is not a String but an Array")
        }
        return value.asString()
    }

    fun getAsStringList(key: String): List<String>? {
        val value: Value? = this.values[key]
        return when {
            value == null -> emptyList()
            value.isArray() -> value.asArray().stream().map(Value::asString).collect(Collectors.toList())
            else -> Collections.singletonList(value.asString())
        }
    }

    fun getAsStringMap(key: String): Map<String, String>? {
        val value: Value? = this.values[key]
        return when {
            value == null -> emptyMap()
            value.isMap() -> value.asMap().map { it.key to it.value.asString() }.toMap()
            else -> throw UnsupportedOperationException()
        }
    }

    fun get(key: String): Any {
        return toObject(this.values[key])
    }

    private fun toObject(value: Value?): Any {
        if (value == null) return Any()
        return when {
            value.isMap() -> value.asMap().map { it.key to toObject(it.value) }.toMap()
            value.isArray() -> value.asArray().stream().map(this::toObject).collect(Collectors.toList())
            else -> value.asString()
        }
    }

    override fun toString(): String {
        return "DCResource(version=$version, baseUri=$baseUri, type=$type, iri=$iri, uri=${getUri()}, " +
                "encodedType=$encodedType, created=$created, lastModified=$lastModified, createdBy=$createdBy, " +
                "lastModifiedBy=$lastModifiedBy, values=$values)"
    }

    // poor design, but it's just for convenience anyway
    abstract class Value {
        abstract fun isArray(): Boolean
        abstract fun isMap(): Boolean
        abstract fun asString(): String
        abstract fun asArray(): List<Value>
        abstract fun asMap(): Map<String,Value>
        open fun isNull(): Boolean = false
        fun isString(): Boolean = !isArray() && !isMap()
    }

    class StringValue(val value: String) : DCResource.Value() {

        override fun isArray(): Boolean = false

        override fun isMap(): Boolean = false

        override fun asString(): String = value

        override fun asArray(): List<Value> = throw UnsupportedOperationException()

        override fun asMap(): Map<String,Value> = throw UnsupportedOperationException()

        override fun isNull(): Boolean = value == null
    }

    class ArrayValue() : DCResource.Value() {

        var values: List<Value> = ArrayList()

        constructor(values: List<Value>): this() {
            this.values = values
        }

        override fun isArray(): Boolean = true

        override fun isMap() = false

        override fun asString() = throw UnsupportedOperationException()

        override fun asArray() = values

        override fun asMap() = throw UnsupportedOperationException()
    }

    class MapValue() : DCResource.Value() {

        var values: Map<String,Value> = HashMap()

        constructor(values: Map<String, Value>): this() {
            this.values = values
        }

        override fun isArray() = false

        override fun isMap() = true

        override fun asString() = throw UnsupportedOperationException()

        override fun asArray() = throw UnsupportedOperationException()

        override fun asMap() = values
    }
}
