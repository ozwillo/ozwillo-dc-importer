package org.ozwillo.dcimporter.model.marchepublic

import org.ozwillo.dcimporter.model.datacore.DCBusinessResourceLight
import org.ozwillo.dcimporter.util.DCUtils
import java.time.LocalDateTime

data class Consultation(
        val idPouvoirAdjudicateur: String,
        val reference: String,
        val objet: String,
        val datePublication: LocalDateTime,
        val dateCloture: LocalDateTime,
        val refInterne: String?, /* ensure it is needed */
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
    fun toDcObject(baseUri: String): DCBusinessResourceLight {
        val resourceLight = DCBusinessResourceLight(DCUtils.getUri(baseUri, "marchepublic:consultation_0",
                "$idPouvoirAdjudicateur/$refInterne"))
        resourceLight.setStringValue("mpconsultation:idPouvoirAdjudicateur", idPouvoirAdjudicateur)
        resourceLight.setStringValue("mpconsultation:reference", reference)
        resourceLight.setStringValue("mpconsultation:objet", objet)
        resourceLight.setDateTimeValue("mpconsultation:datePublication", datePublication)
        resourceLight.setDateTimeValue("mpconsultation:dateCloture", dateCloture)
        refInterne?.let { resourceLight.setStringValue("mpconsultation:refInterne", refInterne) }
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
    ACCORD_CADRE,
    DSP,
    AUTRE
}

enum class TypeMarcheType {
    PUBLIQUE,
    ORDO_2005,
    PRIVE
}

enum class TypePrestationType {
    TRAVAUX,
    SERVICES,
    FOURNITURES,
    AUTRES
}