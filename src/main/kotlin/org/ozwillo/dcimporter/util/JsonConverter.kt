package org.ozwillo.dcimporter.util

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.ozwillo.dcimporter.model.datacore.DCBusinessResourceLight
import org.ozwillo.dcimporter.model.datacore.DCResourceLight
import org.slf4j.LoggerFactory

class JsonConverter{

    companion object {

        private val LOGGER = LoggerFactory.getLogger(JsonConverter::class.java)

        fun objectToJson(resource: DCResourceLight):String{

            val mapper = jacksonObjectMapper()

            var jsonStr = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(resource)
            LOGGER.debug("conversion json : {}", jsonStr)

            return jsonStr
        }

        fun jsonToobject(input:String):DCBusinessResourceLight{

            val mapper = jacksonObjectMapper()
            mapper.findAndRegisterModules()

            var resource:DCBusinessResourceLight = mapper.readValue(input)


            LOGGER.debug("Conversion vers objet {}", resource)

            return resource
        }
    }


}