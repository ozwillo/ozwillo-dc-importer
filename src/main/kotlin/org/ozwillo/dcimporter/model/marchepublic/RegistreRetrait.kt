package org.ozwillo.dcimporter.model.marchepublic

import org.ozwillo.dcimporter.model.datacore.DCBusinessResourceLight
import org.ozwillo.dcimporter.util.DCUtils
import org.ozwillo.dcimporter.util.MSUtils
import org.ozwillo.dcimporter.util.soap.response.parsing.ResponseObject
import java.time.Instant
import java.time.LocalDateTime
import java.util.*

data class RegistreRetrait(override val cle: String,
                           override val siret: String,
                           override val consultationReference: String,
                           override val pieceId: String,
                           override val nomPiece: String,
                           override val libellePiece: String,
                           override val dateDebut: LocalDateTime,
                           override val dateFin: LocalDateTime,
                           override val personne: Personne? = null): Registre(){

    fun toDcObject(baseUri: String, msCle: String, clePersonne: String, pieceUri: String): DCBusinessResourceLight {
        val resourceLight = DCBusinessResourceLight(DCUtils.getUri(baseUri, MSUtils.RETRAIT_TYPE,
                "FR/$siret/$consultationReference/$msCle"))
        val consultationUri = DCUtils.getUri(baseUri, MSUtils.CONSULTATION_TYPE, "FR/$siret/$consultationReference")
        val personneUri = DCUtils.getUri(baseUri, MSUtils.PERSONNE_TYPE, clePersonne)
        resourceLight.setStringValue("mpretrait:mscle", msCle)
        resourceLight.setStringValue("mpretrait:consultation", consultationUri)
        resourceLight.setStringValue("mpretrait:piece", pieceUri)
        resourceLight.setStringValue("mpretrait:personne", personneUri)
        resourceLight.setStringValue("mpretrait:nomPiece", nomPiece)
        resourceLight.setStringValue("mpretrait:libellePiece", libellePiece)
        resourceLight.setDateTimeValue("mpretrait:dateDebut", dateDebut)
        resourceLight.setDateTimeValue("mpretrait:dateFin", dateFin)

        return resourceLight
    }

    companion object {

        fun fromSoapObject(responseObject: ResponseObject, siret: String, consultationReference: String, pieceId: String): RegistreRetrait {

            val personne = Personne.fromSoapObject(responseObject)

            return RegistreRetrait(cle = responseObject.properties!![0].value!!,
                    siret = siret,
                    consultationReference = consultationReference,
                    pieceId = pieceId,
                    nomPiece = responseObject.properties[2].value!!,
                    libellePiece = responseObject.properties[3].value!!,
                    dateDebut = LocalDateTime.ofInstant(Instant.ofEpochSecond((responseObject.properties[7].value!!).toLong()), TimeZone.getDefault().toZoneId()),
                    dateFin = LocalDateTime.ofInstant(Instant.ofEpochSecond((responseObject.properties[9].value!!).toLong()), TimeZone.getDefault().toZoneId()),
                    personne = personne
            )
        }

        fun fromDCObject(dcRegistreRetrait: DCBusinessResourceLight): RegistreRetrait {

            return RegistreRetrait(cle = dcRegistreRetrait.getStringValue("mpretrait:mscle"),
                    siret = dcRegistreRetrait.getIri().split("/")[1],
                    consultationReference = dcRegistreRetrait.getIri().split("/")[2],
                    pieceId = (dcRegistreRetrait.getStringValue("mpretrait:piece")),
                    nomPiece = dcRegistreRetrait.getStringValue("mpretrait:nomPiece"),
                    libellePiece = dcRegistreRetrait.getStringValue("mpretrait:libellePiece"),
                    dateDebut = dcRegistreRetrait.getDateValue("mpretrait:dateDebut"),
                    dateFin = dcRegistreRetrait.getDateValue("mpretrait:dateFin"))
        }
    }
}