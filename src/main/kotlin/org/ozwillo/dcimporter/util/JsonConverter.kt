package org.ozwillo.dcimporter.util

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.ozwillo.dcimporter.model.datacore.DCResource

object JsonConverter {

    fun objectToJson(resource: DCResource): String {

        val mapper = jacksonObjectMapper()
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(resource)
    }

    fun jsonToObject(input: String): DCResource {

        val mapper = jacksonObjectMapper()
        mapper.findAndRegisterModules()
        return mapper.readValue(input)
    }
}
