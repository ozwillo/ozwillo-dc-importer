package org.ozwillo.dcimporter.util

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.ozwillo.dcimporter.model.datacore.DCBusinessResourceLight
import org.ozwillo.dcimporter.model.datacore.DCResourceLight
import org.slf4j.LoggerFactory

object JsonConverter{

        private val LOGGER = LoggerFactory.getLogger(JsonConverter::class.java)

        fun objectToJson(resource: DCResourceLight):String{

            val mapper = jacksonObjectMapper()

            val jsonStr = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(resource)
            LOGGER.debug("conversion json : {}", jsonStr)

            return jsonStr
        }

        fun jsonToObject(input:String):DCBusinessResourceLight{

            val mapper = jacksonObjectMapper()
            mapper.findAndRegisterModules()

            val resource:DCBusinessResourceLight = mapper.readValue(input)

            LOGGER.debug("Conversion vers objet {}", resource)

            return resource
        }
}