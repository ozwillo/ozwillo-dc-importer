package org.ozwillo.dcimporter.model.marchepublic

import org.ozwillo.dcimporter.model.datacore.DCBusinessResourceLight
import org.ozwillo.dcimporter.util.DCUtils
import java.nio.charset.Charset
import java.util.*

data class Piece(
        val uuid: String = UUID.randomUUID().toString(),
        val uuidLot: String?,
        val libelle: String,
        val aapc: Boolean,
        val ordre: Int,
        val nom: String,
        val extension: String,
        val contenu: ByteArray,
        val poids: Int
) {
    fun toDcObject(baseUri: String, siret: String, reference: String, uuid: String): DCBusinessResourceLight {
        val resourceLight = DCBusinessResourceLight(DCUtils.getUri(baseUri, "marchepublic:piece_0",
                "FR/$siret/$reference/$uuid"))
        val consultationUri = DCUtils.getUri(baseUri, "marchepublic:consultation_0", "FR/$siret/$reference")
        resourceLight.setStringValue("mppiece:consultation", consultationUri)
        resourceLight.setStringValue("mppiece:uuid", uuid)
        uuidLot?.let { resourceLight.setStringValue("mppiece:lot", DCUtils.getUri(baseUri, "marchepublic:lot_0", "FR/$siret/$reference/$uuidLot")) }
        resourceLight.setStringValue("mppiece:libelle", libelle)
        resourceLight.setBooleanValue("mppiece:aapc", aapc)
        resourceLight.setIntegerValue("mppiece:ordre", ordre)
        resourceLight.setStringValue("mppiece:nom", nom)
        resourceLight.setStringValue("mppiece:extension", extension)
        resourceLight.setStringValue("mppiece:contenu", contenu.contentToString())
        resourceLight.setIntegerValue("mppiece:poids", poids)

        return resourceLight
    }

    fun toDcObject(baseUri: String, siret: String, reference: String) = toDcObject(baseUri, siret, reference, uuid)

    companion object {

        fun toPiece(dcPiece: DCBusinessResourceLight): Piece =
                Piece(uuid = dcPiece.getStringValue("mppiece:uuid"),
                        uuidLot = dcPiece.getStringValue("mppiece:lot"),
                        libelle = dcPiece.getStringValue("mppiece:libelle"),
                        aapc = dcPiece.getBooleanValue("mppiece:aapc"),
                        ordre = dcPiece.getIntValue("mppiece:ordre"),
                        nom = dcPiece.getStringValue("mppiece:nom"),
                        extension = dcPiece.getStringValue("mppiece:extension"),
                        contenu = dcPiece.getStringValue("mppiece:contenu").toByteArray(Charset.defaultCharset()),
                        poids = dcPiece.getIntValue("mppiece:poids"))
    }
}