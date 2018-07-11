package org.ozwillo.dcimporter.service.marchesecurise.rabbitMQ

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.ozwillo.dcimporter.model.datacore.DCBusinessResourceLight
import org.ozwillo.dcimporter.model.datacore.DCResourceLight
import org.slf4j.LoggerFactory

class JsonConverter{

    companion object {

        private val LOGGER = LoggerFactory.getLogger(JsonConverter::class.java)

        fun consultationToJson(resource: DCResourceLight):String{

            val mapper = jacksonObjectMapper()

            var jsonStr = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(resource)
            LOGGER.debug("===CONVERSION CONSULTATION VERS JSON=== \n {}", jsonStr)

            return jsonStr
        }

        fun JsonToConsultation(input:String):DCBusinessResourceLight{

            val mapper = jacksonObjectMapper()
            mapper.findAndRegisterModules()

            var resource:DCBusinessResourceLight = mapper.readValue(input)


            LOGGER.debug("===CONVERSION JSON VERS CONSULTATION=== \n {}", resource)

            return resource
        }
    }


}