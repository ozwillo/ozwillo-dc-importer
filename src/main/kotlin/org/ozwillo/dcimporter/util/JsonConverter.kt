package org.ozwillo.dcimporter.util

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.ozwillo.dcimporter.model.datacore.DCBusinessResourceLight
import org.ozwillo.dcimporter.model.datacore.DCResourceLight

object JsonConverter {

    fun objectToJson(resource: DCResourceLight): String {

        val mapper = jacksonObjectMapper()
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(resource)
    }

    fun jsonToObject(input: String): DCBusinessResourceLight {

        val mapper = jacksonObjectMapper()
        mapper.findAndRegisterModules()
        return mapper.readValue(input)
    }
}