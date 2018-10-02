package org.ozwillo.dcimporter.model.sirene

import org.ozwillo.dcimporter.config.DatacoreProperties
import org.ozwillo.dcimporter.model.datacore.DCBusinessResourceLight
import org.ozwillo.dcimporter.util.DCUtils
import org.ozwillo.dcimporter.util.soap.response.parsing.ResponseObject
import org.springframework.beans.factory.annotation.Autowired

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
        resourceLight.setStringValue("adrpost:postName", commune)
        resourceLight.setStringValue("adrpost:country", pays)
        resourceLight.setStringValue("org:country", pays)
        resourceLight.setStringValue("org:legalName", denominationUniteLegale)
        resourceLight.setStringValue("org:regNumber", siret)
        resourceLight.setStringValue("", tel)
        resourceLight.setStringValue("", naf)
        resourceLight.setStringValue("", url)

        return resourceLight
    }

    companion object {

        fun fromSoapObject(responseObject: ResponseObject): Organization{

            val cp = responseObject.responseObject!![0].properties!![3].value!!
            val voie = responseObject.responseObject[0].properties!![1].value!! +
                    if (!responseObject.responseObject[0].properties!![2].value!!.isEmpty())
                        ", ${responseObject.responseObject[0].properties!![2].value!!}"
                    else ""
            val pays = responseObject.responseObject[0].properties!![5].value!!
            val commune = responseObject.responseObject[0].properties!![4].value!!
            val tel = responseObject.responseObject[0].properties!![6].value!!
            val fax = responseObject.responseObject[0].properties!![7].value!!

            return Organization(
                    cp = cp,
                    voie = voie,
                    pays = "http://data.ozwillo.com/dc/type/geocofr:Pays_0/$pays",
                    commune = "http://data.ozwillo.com/dc/type/geocifr:Commune_0/$pays/$pays-${cp.substring(0,2)}/$commune",
                    denominationUniteLegale = responseObject.responseObject[0].properties!![0].value!!,
                    siret = responseObject.responseObject[0].properties!![8].value!!,
                    tel = "telephone : $tel, fax : $fax",
                    naf = responseObject.responseObject[0].properties!![10].value!!,
                    url = responseObject.responseObject[0].properties!![11].value!!
            )
        }

        fun fromDcObject(dcOrg: DCBusinessResourceLight): Organization =
                Organization(
                        cp = dcOrg.getStringValue("adrpost:postCode"),
                        voie = dcOrg.getStringValue("adrpost:streetAndNumber"),
                        commune = dcOrg.getStringValue("adrpost:postName"),
                        pays = dcOrg.getStringValue("org:country"),
                        denominationUniteLegale = dcOrg.getStringValue("org:legalName"),
                        siret = dcOrg.getStringValue("org:regNumber"),
                        tel = dcOrg.getStringValue(""),
                        naf = dcOrg.getStringValue(""),
                        url = dcOrg.getStringValue("")
                )
    }
}