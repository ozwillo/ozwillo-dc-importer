package org.ozwillo.dcimporter.model.marchepublic

import org.ozwillo.dcimporter.model.datacore.DCBusinessResourceLight
import org.ozwillo.dcimporter.model.sirene.Organization
import org.ozwillo.dcimporter.util.DCUtils
import org.ozwillo.dcimporter.util.MSUtils
import org.ozwillo.dcimporter.util.soap.response.parsing.ResponseObject
import java.time.Instant
import java.time.LocalDateTime
import java.util.*

data class RegistreRetrait(override val cle: String,
                           override val siret: String,
                           override val consultationUri: String,
                           val pieceId: String,
                           val nomPiece: String,
                           val libellePiece: String,
                           val dateDebut: LocalDateTime,
                           val dateFin: LocalDateTime,
                           var personne: Personne? = null,
                           override var entreprise: Organization): Registre(cle = cle, siret = siret, consultationUri = consultationUri, entreprise = entreprise){

    fun toDcObject(baseUri: String, msCle: String, clePersonne: String, pieceUri: String): DCBusinessResourceLight {
        val consultationReference = consultationUri.substringAfterLast("/")
        val siretOrgfr = consultationUri.split("/")[7]
        val resourceLight = DCBusinessResourceLight(DCUtils.getUri(baseUri, MSUtils.RETRAIT_TYPE,
                "FR/$siretOrgfr/$consultationReference/$msCle"))

        val personneUri = DCUtils.getUri(baseUri, MSUtils.PERSONNE_TYPE, clePersonne)
        val entrepriseUri = DCUtils.getUri(baseUri, "orgfr:Organisation_0", "FR/$siret")

        resourceLight.setStringValue("mpretrait:mscle", msCle)
        resourceLight.setStringValue("mpretrait:consultation", consultationUri)
        if(!siret.isEmpty()) resourceLight.setStringValue("mpretrait:entreprise", entrepriseUri) //siret not always present in Marchés Sécurisés Reponse or retraits
        resourceLight.setStringValue("mpretrait:piece", pieceUri)
        resourceLight.setStringValue("mpretrait:personne", personneUri)
        resourceLight.setStringValue("mpretrait:nomPiece", nomPiece)
        resourceLight.setStringValue("mpretrait:libellePiece", libellePiece)
        resourceLight.setDateTimeValue("mpretrait:dateDebut", dateDebut)
        resourceLight.setDateTimeValue("mpretrait:dateFin", dateFin)

        return resourceLight
    }

    companion object {

        fun fromSoapObject(baseUri: String, responseObject: ResponseObject, consultationUri: String, pieceId: String): RegistreRetrait {

            val personne = Personne.fromSoapObject(responseObject)
            val organization = Organization.fromSoapObject(baseUri, responseObject)

            return RegistreRetrait(cle = responseObject.properties!![0].value!!,
                    siret = responseObject.responseObject!![0].properties!![8].value!!,
                    consultationUri = consultationUri,
                    pieceId = pieceId,
                    nomPiece = responseObject.properties[2].value!!,
                    libellePiece = responseObject.properties[3].value!!,
                    dateDebut = LocalDateTime.ofInstant(Instant.ofEpochSecond((responseObject.properties[7].value!!).toLong()), TimeZone.getDefault().toZoneId()),
                    dateFin = LocalDateTime.ofInstant(Instant.ofEpochSecond((responseObject.properties[9].value!!).toLong()), TimeZone.getDefault().toZoneId()),
                    personne = personne,
                    entreprise = organization
            )
        }

        fun fromDCObject(dcRegistreRetrait: DCBusinessResourceLight): RegistreRetrait {

            return RegistreRetrait(cle = dcRegistreRetrait.getStringValue("mpretrait:mscle"),
                    siret = (dcRegistreRetrait.getStringValue("mpretrait:entreprise")).substringAfterLast("/"),
                    consultationUri = dcRegistreRetrait.getStringValue("mpretrait:consultation"),
                    pieceId = (dcRegistreRetrait.getStringValue("mpretrait:piece")),
                    nomPiece = dcRegistreRetrait.getStringValue("mpretrait:nomPiece"),
                    libellePiece = dcRegistreRetrait.getStringValue("mpretrait:libellePiece"),
                    dateDebut = dcRegistreRetrait.getDateValue("mpretrait:dateDebut"),
                    dateFin = dcRegistreRetrait.getDateValue("mpretrait:dateFin"),
                    entreprise = Organization(siret = (dcRegistreRetrait.getStringValue("mpretrait:entreprise")).substringAfterLast("/")))
        }
    }
}