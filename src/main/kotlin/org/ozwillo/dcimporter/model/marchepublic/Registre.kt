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

    open fun toDcObject(baseUri: String, siret: String, reference: String, msCle: String): DCBusinessResourceLight {
       return DCBusinessResourceLight("")
    }

    open fun fromSoapObject(responseObject: ResponseObject): Registre =
                Registre()

    open fun fromDCObject(dcRegistreReponse: DCBusinessResourceLight): Registre {
        return Registre()
    }
}