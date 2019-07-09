package org.ozwillo.dcimporter.model.marchepublic

import org.ozwillo.dcimporter.model.datacore.DCResource
import org.ozwillo.dcimporter.util.DCUtils
import java.util.*

data class Lot(
    val libelle: String,
    val ordre: Int,
    val numero: Int,
    val uuid: String = UUID.randomUUID().toString()
) {
    fun toDcObject(baseUri: String, siret: String, reference: String, uuid: String): DCResource {
        val resourceLight = DCResource(
            DCUtils.getUri(
                baseUri, "marchepublic:lot_0",
                "FR/$siret/$reference/$uuid"
            )
        )
        val consultationUri = DCUtils.getUri(baseUri, "marchepublic:consultation_0", "FR/$siret/$reference")
        resourceLight.setStringValue("mplot:uuid", uuid)
        resourceLight.setStringValue("mplot:consultation", consultationUri)
        resourceLight.setStringValue("mplot:libelle", libelle)
        resourceLight.setIntegerValue("mplot:ordre", ordre)
        resourceLight.setIntegerValue("mplot:numero", numero)

        return resourceLight
    }

    fun toDcObject(baseUri: String, siret: String, reference: String) = toDcObject(baseUri, siret, reference, uuid)

    companion object {

        fun toLot(dcLot: DCResource): Lot =
            Lot(
                libelle = dcLot.getStringValue("mplot:libelle"),
                ordre = dcLot.getIntValue("mplot:ordre"),
                numero = dcLot.getIntValue("mplot:numero"),
                uuid = dcLot.getStringValue("mplot:uuid")
            )
    }
}
