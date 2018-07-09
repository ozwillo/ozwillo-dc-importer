package org.ozwillo.dcimporter.service.marchesecurise.rabbitMQ

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.ozwillo.dcimporter.model.marchepublic.Consultation
import org.ozwillo.dcimporter.web.MarchePublicHandler
import org.slf4j.LoggerFactory
import java.sql.Timestamp
import java.time.LocalDateTime

class JsonConverter{

    companion object {

        private val LOGGER = LoggerFactory.getLogger(JsonConverter::class.java)

        @JvmStatic
        fun consultationToJson(consultation: Consultation):String{

            val mapper = jacksonObjectMapper()

            var jsonStr = mapper.writeValueAsString(consultation)
            jsonStr = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(consultation)
            LOGGER.debug("===CONVERSION CONSULTATION VERS JSON=== \n {}", jsonStr)

            return jsonStr
        }

        @JvmStatic
        fun JsonToConsultation(input:String):Consultation{

            val mapper = jacksonObjectMapper()
            //mapper.findAndRegisterModules()

            var consultation:Consultation = mapper.readValue<Consultation>(input)

            LOGGER.debug("===CONVERSION JSON VERS CONSULTATION=== \n {}")

            return consultation
        }
    }


}