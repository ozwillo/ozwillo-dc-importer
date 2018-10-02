package org.ozwillo.dcimporter.model.marchepublic

import org.ozwillo.dcimporter.model.datacore.DCBusinessResourceLight
import org.ozwillo.dcimporter.model.sirene.Organization
import org.ozwillo.dcimporter.util.DCUtils
import org.ozwillo.dcimporter.util.MSUtils
import org.ozwillo.dcimporter.util.soap.response.parsing.ResponseObject
import java.time.Instant
import java.time.LocalDateTime
import java.util.*

data class RegistreReponse(val cleReponse: String,
                    val nomContact: String,
                    val emailContact: String,
                    val dateDepot: LocalDateTime,
                    val poids: Int){

    fun toDcObject(baseUri: String, siret: String, reference: String, msCle: String): DCBusinessResourceLight {
        val resourceLight = DCBusinessResourceLight(DCUtils.getUri(baseUri, MSUtils.RESPONSE_TYPE,
                "FR/$siret/$reference/$msCle"))
        val consultationUri = DCUtils.getUri(baseUri, MSUtils.CONSULTATION_TYPE, "FR/$siret/$reference")
        resourceLight.setStringValue("mpreponse:mscle", msCle)
        resourceLight.setStringValue("mpreponse:consultation", consultationUri)
        resourceLight.setStringValue("mpreponse:contact", nomContact)
        resourceLight.setStringValue("mpreponse:email", emailContact)
        resourceLight.setDateTimeValue("mpreponse:dateDepot", dateDepot)
        resourceLight.setIntegerValue("mpreponse:poids", poids)

        return resourceLight
    }

    companion object {

        fun fromSoapObject(responseObject: ResponseObject): RegistreReponse =
                RegistreReponse(cleReponse = responseObject.properties!![0].value!!,
                        nomContact = responseObject.properties[4].value!!,
                        emailContact = responseObject.properties[5].value!!,
                        dateDepot = LocalDateTime.ofInstant(Instant.ofEpochSecond((responseObject.properties[6].value!!).toLong()), TimeZone.getDefault().toZoneId()),
                        poids = responseObject.properties[8].value!!.toInt()
                )

        fun fromDCObject(dcRegistreReponse: DCBusinessResourceLight): RegistreReponse {

            return RegistreReponse(cleReponse = dcRegistreReponse.getStringValue("mpreponse:mscle"),
                    nomContact = dcRegistreReponse.getStringValue("mpreponse:contact"),
                    emailContact = dcRegistreReponse.getStringValue("mpreponse:email"),
                    dateDepot = dcRegistreReponse.getDateValue("mpreponse:dateDepot"),
                    poids = dcRegistreReponse.getIntValue("mpreponse:poids"))
        }
    }
}

enum class Ordre(val value: String){
    DATE_PREMIER_RETRAIT("date_retrait"),
    DATE_DERNIER_RETRAIT("date_retrait_r"),
    ENTREPRISE("denomination_ent")
}

enum class SensOrdre{
    ASC,
    DESC
}