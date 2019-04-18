package org.ozwillo.dcimporter.model.marchepublic

import org.ozwillo.dcimporter.model.datacore.DCResource
import org.ozwillo.dcimporter.model.sirene.Organization

open class Registre(
    open val cle: String,
    open val siret: String,
    open val consultationUri: String,
    open val entreprise: Organization
) {

    open fun toDcObject(baseUri: String, msCle: String): DCResource {
        return DCResource("")
    }
}

enum class Ordre(val value: String) {
    DATE_PREMIER_RETRAIT("date_retrait"),
    DATE_DERNIER_RETRAIT("date_retrait_r"),
    ENTREPRISE("denomination_ent")
}

enum class SensOrdre {
    ASC,
    DESC
}
