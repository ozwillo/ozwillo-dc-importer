package org.ozwillo.dcimporter.service.marchesecurise.rabbitMQ

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.ozwillo.dcimporter.model.datacore.DCBusinessResourceLight
import org.ozwillo.dcimporter.model.datacore.DCResourceLight
import org.ozwillo.dcimporter.model.marchepublic.Consultation
import org.ozwillo.dcimporter.model.rabbitmq.marchesecurise.ConsultationMessage
import org.ozwillo.dcimporter.service.DatacoreService
import org.ozwillo.dcimporter.web.MarchePublicHandler
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import java.sql.Timestamp
import java.time.LocalDateTime

class JsonConverter{

    companion object {

        private val LOGGER = LoggerFactory.getLogger(JsonConverter::class.java)

        fun consultationToJson(consultationMessage: ConsultationMessage):String{

            val mapper = jacksonObjectMapper()

            var jsonStr = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(consultationMessage)
            LOGGER.debug("===CONVERSION CONSULTATION VERS JSON=== \n {}", jsonStr)

            return jsonStr
        }

        fun JsonToConsultation(input:String):ConsultationMessage{

            val mapper = jacksonObjectMapper()
            mapper.findAndRegisterModules()

            var consultationMessage:ConsultationMessage = mapper.readValue(input)


            LOGGER.debug("===CONVERSION JSON VERS CONSULTATION=== \n {}", consultationMessage)

            return consultationMessage
        }
    }


}