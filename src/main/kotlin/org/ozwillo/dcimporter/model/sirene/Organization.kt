package org.ozwillo.dcimporter.model.sirene

import org.ozwillo.dcimporter.model.datacore.DCBusinessResourceLight
import org.ozwillo.dcimporter.model.datacore.I18nOrgDenomination
import org.ozwillo.dcimporter.util.DCUtils
import org.ozwillo.dcimporter.util.soap.response.parsing.ResponseObject

data class Organization(val cp: String = "",
                        val voie: String = "",
                        val commune: String = "",
                        val pays: String = "FR",
                        val denominationUniteLegale: String = "",
                        val siret: String,
                        val tel: String = "",
                        val naf: String = "",
                        val url: String = ""
){

    fun toDcObject(baseUri: String, siret: String): DCBusinessResourceLight{
        val resourceLight = DCBusinessResourceLight(DCUtils.getUri(baseUri, "orgfr:Organisation_0", "FR/$siret"))
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

    companion object {

        fun fromSoapObject(baseUri: String, responseObject: ResponseObject): Organization{

            val index = responseObject.responseObject!!.indexOf(responseObject.responseObject.find { p -> p.type == "entreprise" })
            val cp = responseObject.responseObject[index].properties!![3].value!!
            val voie = responseObject.responseObject[index].properties!![1].value!! +
                    if (!responseObject.responseObject[index].properties!![2].value!!.isEmpty())
                        ", ${responseObject.responseObject[index].properties!![2].value!!}"
                    else ""
            val pays = responseObject.responseObject[index].properties!![5].value!!
            val commune = responseObject.responseObject[index].properties!![4].value!!
            val tel = responseObject.responseObject[index].properties!![6].value!!

            return Organization(
                    cp = cp,
                    voie = voie,
                    pays = DCUtils.getUri(baseUri, "geocofr:Pays_0", pays),
                    commune = DCUtils.getUri(baseUri, "geocifr:Commune_0", "$pays/$pays-${cp.substring(0,2)}/$commune"),
                    denominationUniteLegale = responseObject.responseObject[index].properties!![0].value!!,
                    siret = responseObject.responseObject[index].properties!![8].value!!,
                    tel = tel,
                    naf = responseObject.responseObject[index].properties!![10].value!!,
                    url = responseObject.responseObject[index].properties!![11].value!!
            )
        }

        fun fromDcObject(dcOrg: DCBusinessResourceLight): Organization =
                Organization(
                        cp = dcOrg.getStringValue("adrpost:postCode"),
                        voie = dcOrg.getStringValue("adrpost:streetAndNumber"),
                        commune = dcOrg.getStringValue("adrpost:postName"),
                        pays = dcOrg.getStringValue("org:country"),
                        denominationUniteLegale = dcOrg.getI18nFieldValueFromList(dcOrg.getStringListValue("org:legalName") as List<I18nOrgDenomination>, "fr"),
                        siret = dcOrg.getStringValue("org:regNumber"),
                        tel = dcOrg.getStringValue("org:phoneNumber"),
                        url = dcOrg.getStringValue("org:webSite")
                )
    }
}