package org.ozwillo.dcimporter.model.sirene

import org.ozwillo.dcimporter.model.datacore.DCResource
import org.ozwillo.dcimporter.util.DCUtils

data class Organization(
    val uri: String = "",
    val cp: String = "",
    val voie: String = "",
    val commune: String = "",
    val pays: String = "FR",
    val denominationUniteLegale: String = "",
    val siret: String,
    val tel: String = "",
    val naf: String = "",
    val url: String = ""
) {

    fun toDcObject(baseUri: String, siret: String): DCResource {
        val resourceLight = DCResource(DCUtils.getUri(baseUri, "orgfr:Organisation_0", "FR/$siret"))
        resourceLight.setStringValue("adrpost:postCode", cp)
        resourceLight.setStringValue("adrpost:streetAndNumber", voie)
        resourceLight.setStringValue("adrpost:country", pays)
        resourceLight.setStringValue("org:country", pays)
        resourceLight.setStringValue("org:legalName", denominationUniteLegale)
        resourceLight.setStringValue("org:regNumber", siret)
        resourceLight.setStringValue("org:phoneNumber", tel)
        resourceLight.setStringValue("org:webSite", url)

        return resourceLight
    }
}
