package org.ozwillo.dcimporter.service.marchesecurise.rabbitMQ

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.ozwillo.dcimporter.model.marchepublic.Consultation

class JsonConverter{

    companion object {
        @JvmStatic
        fun consultationToJson(consultation: Consultation):String{

            val mapper = jacksonObjectMapper()

            var jsonStr = mapper.writeValueAsString(consultation)
            println(jsonStr)
            jsonStr = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(consultation)
            println(jsonStr)

            return jsonStr
        }
    }


}