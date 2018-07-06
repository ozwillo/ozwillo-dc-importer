package org.ozwillo.dcimporter.service.marchesecurise.rabbitMQ

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.ozwillo.dcimporter.model.marchepublic.Consultation
import java.sql.Timestamp
import java.time.LocalDateTime

class JsonConverter{

    companion object {
        @JvmStatic
        fun consultationToJson(consultation: Consultation):String{

            val mapper = jacksonObjectMapper()

            var jsonStr = mapper.writeValueAsString(consultation)
            println("===========CONVERSION OBJET VERS JSON==================")
            jsonStr = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(consultation)
            println(jsonStr)

            return jsonStr
        }

        @JvmStatic
        fun JsonToConsultation(input:String):Consultation{

            val mapper = jacksonObjectMapper()
            //mapper.findAndRegisterModules()

            var consultation:Consultation = mapper.readValue<Consultation>(input)


            println("==================CONVERSION JSON VERS OBJET=====================")
            println(consultation)

            return consultation
        }
    }


}