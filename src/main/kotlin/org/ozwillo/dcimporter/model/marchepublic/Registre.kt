package org.ozwillo.dcimporter.model.marchepublic

import org.ozwillo.dcimporter.model.datacore.DCBusinessResourceLight
import org.ozwillo.dcimporter.util.soap.response.parsing.ResponseObject
import java.time.LocalDateTime

open class Registre{
    open val cle: String? = null
    open val nomContact: String? = null
    open val emailContact: String? = null
    open val dateDepot: LocalDateTime? = null
    open val poids: Int? = null
    open val siret: String? = null
    open val consultationReference: String? = null
    open val pieceId: String? = null
    open val nomPiece: String? = null
    open val libellePiece: String? = null
    open val dateDebut: LocalDateTime? = null
    open val dateFin: LocalDateTime? = null
    open val personne: Personne? = null

    open fun toDcObject(baseUri: String, msCle: String): DCBusinessResourceLight {
       return DCBusinessResourceLight("")
    }

    open fun fromSoapObject(responseObject: ResponseObject): Registre =
                Registre()

    open fun fromDCObject(dcRegistreReponse: DCBusinessResourceLight): Registre {
        return Registre()
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