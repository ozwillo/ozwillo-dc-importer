package org.ozwillo.dcimporter.model.marchepublic

import org.ozwillo.dcimporter.model.datacore.DCBusinessResourceLight
import org.ozwillo.dcimporter.util.DCUtils
import java.time.LocalDateTime

data class Consultation(
        val reference: String,
        val objet: String,
        val datePublication: LocalDateTime,
        val dateCloture: LocalDateTime,
        val finaliteMarche: FinaliteMarcheType,
        val typeMarche: TypeMarcheType,
        val typePrestation: TypePrestationType,
        val departementsPrestation: List<Int>,
        val passation: String,
        val informatique: Boolean,
        val passe: String?,
        val emails: List<String>,
        val enLigne: Boolean,
        val alloti: Boolean,
        val invisible: Boolean,
        val nbLots: Int
) {
    fun toDcObject(baseUri: String, siret: String): DCBusinessResourceLight {
        val resourceLight = DCBusinessResourceLight(DCUtils.getUri(baseUri, "marchepublic:consultation_0",
                "$siret/$reference"))
        val organizationUri = DCUtils.getUri(baseUri, "org:Organization_0", "FR/$siret")
        resourceLight.setStringValue("mpconsultation:organization", organizationUri)
        resourceLight.setStringValue("mpconsultation:reference", reference)
        resourceLight.setStringValue("mpconsultation:objet", objet)
        resourceLight.setDateTimeValue("mpconsultation:datePublication", datePublication)
        resourceLight.setDateTimeValue("mpconsultation:dateCloture", dateCloture)
        resourceLight.setStringValue("mpconsultation:finaliteMarche", finaliteMarche.toString())
        resourceLight.setStringValue("mpconsultation:typeMarche", typeMarche.toString())
        resourceLight.setStringValue("mpconsultation:typePrestation", typePrestation.toString())
        resourceLight.setListValue("mpconsultation:departementsPrestation", departementsPrestation)
        resourceLight.setStringValue("mpconsultation:passation", passation)
        resourceLight.setStringValue("mpconsultation:informatique", informatique.toString())
        passe?.let { resourceLight.setStringValue("mpconsultation:passe", passe) }
        resourceLight.setListValue("mpconsultation:emails", emails)
        resourceLight.setStringValue("mpconsultation:enLigne", enLigne.toString())
        resourceLight.setStringValue("mpconsultation:alloti", alloti.toString())
        resourceLight.setStringValue("mpconsultation:invisible", invisible.toString())
        resourceLight.setStringValue("mpconsultation:nbLots", nbLots.toString())

        return resourceLight
    }
}

enum class FinaliteMarcheType {
    MARCHE,
    ACCORD,
    DSP,
    AUTRE
}

enum class TypeMarcheType {
    PUBLIC,
    ORDONNANCE2005,
    PRIVE,
    AUTRE
}

enum class TypePrestationType {
    TRAVAUX,
    SERVICES,
    FOURNITURES,
    AUTRES
}