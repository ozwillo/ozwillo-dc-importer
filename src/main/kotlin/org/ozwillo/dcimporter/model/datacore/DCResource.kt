package org.ozwillo.dcimporter.model.datacore

import com.fasterxml.jackson.annotation.*
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

typealias I18nOrgDenomination = HashMap<String, String>
typealias DCResourceURI = String

@JsonIgnoreProperties("@type")
class DCResource(
    private val uri: String,
    @JsonAnySetter private var values: Map<String, Any> = HashMap()
) {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(DCResource::class.java)

        const val dcTypeMidfix = "/dc/type/"
    }

    @JsonIgnore
    val df = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSVV")

    constructor(datacoreBaseUri: String, type: DCModelType, iri: String) : this("$datacoreBaseUri/${type.encodeUriPathSegment()}/$iri")

    @JsonProperty("@id")
    fun getUri(): String {
        return uri
    }

    @JsonIgnore
    fun getIri(): String {
        val modelTypeIndex = uri.indexOf(dcTypeMidfix) + dcTypeMidfix.length
        val idSlashIndex = uri.indexOf('/', modelTypeIndex)

        return uri.substring(idSlashIndex + 1)
    }

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

    fun setFloatValue(key: String, value: Float) {
        this.values = this.values.plus(Pair(key, value))
    }

    fun setDoubleValue(key: String, value: Double) {
        this.values = this.values.plus(Pair(key, value))
    }

    fun setDateTimeValue(key: String, value: LocalDateTime) {
        val zonedDateTime = ZonedDateTime.of(value, ZoneOffset.UTC)
        val formattedDate = (zonedDateTime.format(df))
        this.values = this.values.plus(Pair(key, formattedDate))
    }

    fun setListValue(key: String, values: List<Any>) {
        this.values = this.values.plus(Pair(key, values))
    }

    fun getStringValue(s: String): String = values[s]?.let { it as String }.orEmpty()

    fun getIntValue(s: String): Int = values[s] as Int

    fun getFloatValue(s: String): Float? = values[s] as? Float

    fun getBooleanValue(s: String): Boolean = values[s] as Boolean

    fun getDateValue(s: String): LocalDateTime = LocalDateTime.parse(values[s] as String, df)

    fun getIntListValue(s: String): List<Int> = values[s] as List<Int>

    fun getStringListValue(s: String): List<String> = values[s] as List<String>

    fun getI18nFieldValueFromList(valueList: List<I18nOrgDenomination>, lang: String): String {
        var nameIndex = 0
        var errorMessage = ""
        var correctedLang = ""

        val result = valueList.firstOrNull { value ->
            val langIndex = when {
                value.values.indexOf(lang) >= 0 -> {
                    correctedLang = "fr"
                    value.values.indexOf(lang)
                }
                value.values.indexOf("en") >= 0 -> {
                    correctedLang = "en"
                    errorMessage = "No FR organization legal name found. EN match taken. Please check database"
                    value.values.indexOf("en")
                }
                else -> {
                    correctedLang = value["l"]!!
                    errorMessage = "No FR or EN organization legal name found. First match taken. Please check database."
                    value.values.indexOf(correctedLang)
                }
            }

            nameIndex = if (langIndex == 0) 1 else 0

            value.values.toTypedArray()[langIndex] == correctedLang
        }
        if (!errorMessage.isEmpty() && result != null) {
            LOGGER.debug("${result.values.toTypedArray()[nameIndex]} : $errorMessage")
        }
        return if (result != null) result.values.toTypedArray()[nameIndex] else ""
    }

    override fun toString(): String {
        return "DCBusinessResourceLight(values=${values.filterKeys { key -> key != "citizenreqem:fileContent" }})"
    }
}
