package org.ozwillo.dcimporter.model.sirene

import org.ozwillo.dcimporter.model.datacore.DCBusinessResourceLight
import org.ozwillo.dcimporter.util.DCUtils

data class Organization(val cp: String,
                        val voie: String,
                        val pays: String,
                        val denominationUniteLegale: String,
                        val siret: String
){
    fun toDcObject(baseUri: String, siret: String): DCBusinessResourceLight{
        val resourceLight = DCBusinessResourceLight(DCUtils.getUri(baseUri, "orgfr:Organisation_0", "FR/$siret"))
        resourceLight.setStringValue("adrpost:postCode", cp)
        resourceLight.setStringValue("adrpost:streetAndNumber", voie)
        resourceLight.setStringValue("adrpost:country", pays)
        resourceLight.setStringValue("org:country", pays)
        resourceLight.setStringValue("org:legalName", denominationUniteLegale)
        resourceLight.setStringValue("org:regNumber", siret)

        return resourceLight
    }

    fun fromDcObject(dcOrg: DCBusinessResourceLight): Organization =
            Organization(
                    cp = dcOrg.getStringValue("adrpost:postCode"),
                    voie = dcOrg.getStringValue("adrpost:streetAndNumber"),
                    pays = dcOrg.getStringValue("org:country"),
                    denominationUniteLegale = dcOrg.getStringValue("org:legalName"),
                    siret = dcOrg.getStringValue("org:regNumber")
            )
}