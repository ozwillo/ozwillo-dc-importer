package org.ozwillo.dcimporter.model.marchepublic

import org.ozwillo.dcimporter.model.datacore.DCBusinessResourceLight
import org.ozwillo.dcimporter.util.DCUtils
import org.ozwillo.dcimporter.util.MSUtils
import org.ozwillo.dcimporter.util.soap.response.parsing.ResponseObject

data class Personne(val cle: String,
                    val genre: String,
                    val nom: String,
                    val prenom: String,
                    val email: String,
                    val telephone: String,
                    val fax: String){

    fun toDcObject(baseUri: String): DCBusinessResourceLight {
        val resourceLight = DCBusinessResourceLight(DCUtils.getUri(baseUri, MSUtils.PERSONNE_TYPE, cle))
        resourceLight.setStringValue("mppersonne:genre", genre)
        resourceLight.setStringValue("mppersonne:nom", nom)
        resourceLight.setStringValue("mppersonne:prenom", prenom)
        resourceLight.setStringValue("mppersonne:email", email)
        resourceLight.setStringValue("mppersonne:tel", telephone)
        resourceLight.setStringValue("mppersonne:fax", fax)

        return resourceLight
    }


    companion object {

        fun fromSoapObject(responseObject: ResponseObject): Personne {

            val index = responseObject.responseObject!!.indexOf(responseObject.responseObject.find { p -> p.type == "personne" })

            return Personne(cle = responseObject.properties!![10].value!!,
                    genre = responseObject.responseObject[index].properties!![0].value!!,
                    nom = responseObject.responseObject[index].properties!![1].value!!,
                    prenom = responseObject.responseObject[index].properties!![2].value!!,
                    email = responseObject.responseObject[index].properties!![3].value!!,
                    telephone = responseObject.responseObject[index].properties!![4].value!!,
                    fax = responseObject.responseObject[index].properties!![5].value!!
            )
        }

        fun fromDCObject(dcPersonne: DCBusinessResourceLight): Personne {

            return Personne( cle = dcPersonne.getIri().substringAfterLast("/"),
                    genre = dcPersonne.getStringValue("mppersonne:genre"),
                    nom = dcPersonne.getStringValue("mppersonne:nom"),
                    prenom = dcPersonne.getStringValue("mppersonne:prenom"),
                    email = dcPersonne.getStringValue("mppersonne:email"),
                    telephone = dcPersonne.getStringValue("mppersonne:tel"),
                    fax = dcPersonne.getStringValue("mppersonne:fax"))
        }

    }
}
