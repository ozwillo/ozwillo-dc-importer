package org.ozwillo.dcimporter.model.marchepublic

import org.ozwillo.dcimporter.model.datacore.DCBusinessResourceLight
import org.ozwillo.dcimporter.model.sirene.Organization
import org.ozwillo.dcimporter.util.DCUtils
import org.ozwillo.dcimporter.util.MSUtils
import org.ozwillo.dcimporter.util.soap.response.parsing.ResponseObject
import java.time.Instant
import java.time.LocalDateTime
import java.util.*

data class RegistreReponse(override val cle: String,
                    val nomContact: String,
                    val emailContact: String,
                    val dateDepot: LocalDateTime,
                    val poids: Int,
                    override var entreprise: Organization,
                    override val siret: String,
                    override val consultationUri: String): Registre(cle = cle, siret = siret, consultationUri = consultationUri, entreprise = entreprise) {

    override fun toDcObject(baseUri: String, msCle: String): DCBusinessResourceLight {

        val consultationReference = consultationUri.substringAfterLast("/")
        val siretOrgfr = consultationUri.split("/")[7]

        val resourceLight = DCBusinessResourceLight(DCUtils.getUri(baseUri, MSUtils.REPONSE_TYPE,
                "FR/$siretOrgfr/$consultationReference/$msCle"))

        val entrepriseUri = DCUtils.getUri(baseUri, "orgfr:Organisation_0", "FR/$siret")

        resourceLight.setStringValue("mpreponse:mscle", msCle)
        resourceLight.setStringValue("mpreponse:consultation", consultationUri)
        if (!siret.isEmpty()) resourceLight.setStringValue("mpreponse:entreprise", entrepriseUri) //siret not always present in Marchés Sécurisés Reponse or retraits
        resourceLight.setStringValue("mpreponse:contact", nomContact)
        resourceLight.setStringValue("mpreponse:email", emailContact)
        resourceLight.setDateTimeValue("mpreponse:dateDepot", dateDepot)
        resourceLight.setIntegerValue("mpreponse:poids", poids)

        return resourceLight
    }

    companion object {

        fun fromSoapObject(baseUri: String, responseObject: ResponseObject, consultationUri: String): RegistreReponse {

            val organization = Organization.fromSoapObject(baseUri, responseObject)

            return RegistreReponse(cle = responseObject.properties!![0].value!!,
                    nomContact = responseObject.properties[4].value!!,
                    emailContact = responseObject.properties[5].value!!,
                    dateDepot = LocalDateTime.ofInstant(Instant.ofEpochSecond((responseObject.properties[6].value!!).toLong()), TimeZone.getDefault().toZoneId()),
                    poids = responseObject.properties[8].value!!.toInt(),
                    entreprise = organization,
                    siret = responseObject.responseObject!![0].properties!![8].value!!,
                    consultationUri = consultationUri
            )
        }

        fun fromDCObject(dcRegistreReponse: DCBusinessResourceLight): RegistreReponse {

            return RegistreReponse(cle = dcRegistreReponse.getStringValue("mpreponse:mscle"),
                    nomContact = dcRegistreReponse.getStringValue("mpreponse:contact"),
                    emailContact = dcRegistreReponse.getStringValue("mpreponse:email"),
                    dateDepot = dcRegistreReponse.getDateValue("mpreponse:dateDepot"),
                    poids = dcRegistreReponse.getIntValue("mpreponse:poids"),
                    siret = (dcRegistreReponse.getStringValue("mpreponse:entreprise")).substringAfterLast("/"),
                    consultationUri = dcRegistreReponse.getStringValue("mpreponse:consultation"),
                    entreprise = Organization(siret = (dcRegistreReponse.getStringValue("mpreponse:entreprise")).substringAfterLast("/")))
        }
    }
}