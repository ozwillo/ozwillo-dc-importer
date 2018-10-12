package org.ozwillo.dcimporter.model.marchepublic

import org.ozwillo.dcimporter.model.datacore.DCBusinessResourceLight
import org.ozwillo.dcimporter.util.soap.response.parsing.ResponseObject

open class Registre{
    open val cle: String? = null
    open val siret: String? = null
    open val consultationUri: String? = null

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