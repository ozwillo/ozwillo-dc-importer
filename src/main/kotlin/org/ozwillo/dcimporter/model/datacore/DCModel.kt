package org.ozwillo.dcimporter.model.datacore

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import java.net.URI
import java.util.Objects

@JsonInclude(JsonInclude.Include.NON_NULL)
class DCModel : Comparable<DCModel> {

    @get:JsonProperty("id")
    @set:JsonProperty("@id")
    var id: URI? = null

    @get:JsonProperty("name")
    @set:JsonProperty("dcmo:name")
    var name: String? = null

    @get:JsonProperty("version")
    @set:JsonProperty("o:version")
    var version: Int? = null

    @get:JsonProperty("project")
    @set:JsonProperty("dcmo:pointOfViewAbsoluteName")
    var project: String? = null

    @get:JsonProperty("fields")
    @set:JsonProperty("dcmo:globalFields")
    var fields: List<DcModelField>? = null

    override fun toString(): String {
        return "DCModel{" +
                "id=" + id +
                ", name='" + name + '\''.toString() +
                ", version=" + version +
                ", projet='" + project + '\''.toString() +
                '}'.toString()
    }

    override fun compareTo(other: DCModel): Int {
        return name!!.compareTo(other.name!!)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val model = other as DCModel?
        return id == model!!.id && version == model.version
    }

    override fun hashCode(): Int {
        return Objects.hash(id, version)
    }

    class DcModelField {

        @get:JsonProperty("name")
        @set:JsonProperty("dcmf:name")
        var name: String? = null
    }
}
