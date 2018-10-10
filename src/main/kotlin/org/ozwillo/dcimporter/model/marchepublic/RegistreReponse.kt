package org.ozwillo.dcimporter.model.marchepublic

import org.ozwillo.dcimporter.model.datacore.DCBusinessResourceLight
import org.ozwillo.dcimporter.util.DCUtils
import org.ozwillo.dcimporter.util.MSUtils
import org.ozwillo.dcimporter.util.soap.response.parsing.ResponseObject
import java.time.Instant
import java.time.LocalDateTime
import java.util.*

data class RegistreReponse(override val cle: String,
                    override val nomContact: String,
                    override val emailContact: String,
                    override val dateDepot: LocalDateTime,
                    override val poids: Int,
                    override val siret: String,
                    override val consultationReference: String): Registre() {

    override fun toDcObject(baseUri: String, msCle: String): DCBusinessResourceLight {
        val resourceLight = DCBusinessResourceLight(DCUtils.getUri(baseUri, MSUtils.REPONSE_TYPE,
                "FR/$siret/$consultationReference/$msCle"))
        val consultationUri = DCUtils.getUri(baseUri, MSUtils.CONSULTATION_TYPE, "FR/$siret/$consultationReference")
        resourceLight.setStringValue("mpreponse:mscle", msCle)
        resourceLight.setStringValue("mpreponse:consultation", consultationUri)
        resourceLight.setStringValue("mpreponse:contact", nomContact)
        resourceLight.setStringValue("mpreponse:email", emailContact)
        resourceLight.setDateTimeValue("mpreponse:dateDepot", dateDepot)
        resourceLight.setIntegerValue("mpreponse:poids", poids)

        return resourceLight
    }

    companion object {

        fun fromSoapObject(responseObject: ResponseObject, siret: String, consultationReference: String): RegistreReponse =
                RegistreReponse(cle = responseObject.properties!![0].value!!,
                        nomContact = responseObject.properties[4].value!!,
                        emailContact = responseObject.properties[5].value!!,
                        dateDepot = LocalDateTime.ofInstant(Instant.ofEpochSecond((responseObject.properties[6].value!!).toLong()), TimeZone.getDefault().toZoneId()),
                        poids = responseObject.properties[8].value!!.toInt(),
                        siret = siret,
                        consultationReference = consultationReference
                )

        fun fromDCObject(dcRegistreReponse: DCBusinessResourceLight): RegistreReponse {

            return RegistreReponse(cle = dcRegistreReponse.getStringValue("mpreponse:mscle"),
                    nomContact = dcRegistreReponse.getStringValue("mpreponse:contact"),
                    emailContact = dcRegistreReponse.getStringValue("mpreponse:email"),
                    dateDepot = dcRegistreReponse.getDateValue("mpreponse:dateDepot"),
                    poids = dcRegistreReponse.getIntValue("mpreponse:poids"),
                    siret = dcRegistreReponse.getIri().split("/")[1],
                    consultationReference = dcRegistreReponse.getIri().split("/")[2])
        }
    }
}