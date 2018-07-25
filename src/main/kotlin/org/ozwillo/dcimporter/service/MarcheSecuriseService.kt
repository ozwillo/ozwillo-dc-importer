package org.ozwillo.dcimporter.service

import org.ozwillo.dcimporter.model.BusinessMapping
import org.ozwillo.dcimporter.model.marchepublic.Consultation
import org.ozwillo.dcimporter.model.marchepublic.Lot
import org.ozwillo.dcimporter.model.marchepublic.Piece
import org.ozwillo.dcimporter.repository.BusinessMappingRepository
import org.ozwillo.dcimporter.service.rabbitMQ.Receiver
import org.ozwillo.dcimporter.util.MSUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.ZoneId
import java.util.*

/*
**      SOAP requests generation and sending to Web Service Marche Securise      **
**          * Consultation
**          * Lot
**          * Piece
*/


@Service
class MarcheSecuriseService {

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(Receiver::class.java)

    }

    @Value("\${marchesecurise.url.updateConsultation}")
    private val updateConsultationUrl = ""


    @Autowired
    private lateinit var businessMappingRepository: BusinessMappingRepository

/*
**  Consultation **
*/

    private fun sendCreateConsultationRequest(login: String, password: String, pa: String, url: String): String {
        val soapMessage = MSUtils.generateCreateConsultationLogRequest(login, password, pa)
        return MSUtils.sendSoap(url, soapMessage)
    }

    //  Default Consultation creation
    private fun createConsultationAndSaveDce(login: String, password: String, pa: String, url: String, consultation: Consultation): String {
        val response = sendCreateConsultationRequest(login, password, pa, url)
        val reference: String = consultation.reference!!

        //saving dce (=consultation id in MS)
        val parseResponse: List<String> = response.split("&lt;propriete nom=\"cle\" statut=\"changed\"&gt;|&lt;/propriete&gt;".toRegex())
        val dce = parseResponse[1]
        val businessMapping = BusinessMapping(applicationName = "MS", businessId = dce, dcId = reference)
        logger.debug("saved BusinessMapping : {}", businessMapping)
        businessMappingRepository!!.save(businessMapping).block()

        return response
    }

    //  Default Consultation creation and update with correct data
    fun createAndUpdateConsultation(login: String, password: String, pa: String, consultation: Consultation, url: String): String {

        var response = ""

        //control of already existing businessMapping with same dcId
        val existingBusinessMappings: BusinessMapping? = businessMappingRepository.findByDcIdAndApplicationName(consultation.reference!!, "MS").block()

        if (existingBusinessMappings == null) {
            createConsultationAndSaveDce(login, password, pa, url, consultation)
            response = updateConsultation(login, password, pa, consultation, updateConsultationUrl)
        } else {
            logger.warn("Resource with ref '{}' already exists", consultation.reference)
            response = "No consultation creation request sent to Marche Securise"
        }
        return response
    }


    //  Current consultation updating only
    fun updateConsultation(login:String, password:String, pa:String, consultation:Consultation, url: String):String{

        //  Consultation data formatter
        val objet = if ((consultation.objet).length > 255) (consultation.objet).substring(0, 255) else consultation.objet
        val enligne = MSUtils.booleanToInt(consultation.enLigne).toString()
        val datePublication = consultation.datePublication.atZone(ZoneId.of("Europe/Paris")).toInstant().epochSecond.toString()
        val dateCloture = consultation.dateCloture.atZone(ZoneId.of("Europe/Paris")).toInstant().epochSecond.toString()
        val reference = consultation.reference.toString()
        val finaliteMarche = (consultation.finaliteMarche).toString().toLowerCase()
        val typeMarche = (consultation.typeMarche).toString().toLowerCase()
        val prestation = (consultation.typePrestation).toString().toLowerCase()
        val passation = consultation.passation
        val alloti = MSUtils.booleanToInt(consultation.alloti).toString()
        val departement = MSUtils.intListToString(consultation.departementsPrestation)
        val email = if ((MSUtils.stringListToString(consultation.emails)).length > 255) (MSUtils.stringListToString(consultation.emails)).substring(0, 255) else MSUtils.stringListToString(consultation.emails)

        //  get consultation dce from BusinessMappingRepository and generate SOAP request
        var soapMessage = ""
        try {
            val savedMonoBusinessMapping = businessMappingRepository!!.findByDcIdAndApplicationName(reference, "MS")
            var dce: String = savedMonoBusinessMapping.block()!!.businessId
            soapMessage = MSUtils.generateModifyConsultationLogRequest(login, password, pa, dce, objet, enligne, datePublication, dateCloture, reference, finaliteMarche, typeMarche, prestation, passation, alloti, departement, email)


            logger.debug("get dce {}", dce)
        } catch (e: Exception) {
            logger.warn("error on finding dce from BusinessMapping")
            e.printStackTrace()
        }

        //  SOAP request sending
        return MSUtils.sendSoap(url, soapMessage)
    }

    fun deleteConsultation(login: String, password: String, pa: String, iri:String, url: String):String{

        val reference = iri.split("/")[2]
        var soapMessage = ""

        try {
            val savedMonoBusinessMapping = businessMappingRepository!!.findByDcIdAndApplicationName(reference, "MS")
            var dce: String = savedMonoBusinessMapping.block()!!.businessId
            soapMessage = MSUtils.generateDeleteConsultationLogRequest(login, password, pa, dce)
            val deletedBusinessMapping = businessMappingRepository.deleteByDcIdAndApplicationName(reference, "MS").subscribe()
            logger.debug("get dce {}, result $deletedBusinessMapping", dce)
        } catch (e: Exception) {
            logger.warn("error on finding dce from BusinessMapping")
            e.printStackTrace()
        }

        return MSUtils.sendSoap(url, soapMessage)
    }


/*
**  Lot **
*/

    fun createLot(login: String, password: String, pa: String, lot: Lot, uri: String, url: String): String {

        // Lot data formatter
        val libelle = if (lot.libelle.length > 255) lot.libelle.substring(0, 255) else lot.libelle
        val ordre = lot.ordre.toString()
        val numero = lot.numero.toString()

        //  Get consultation reference from uri
        val reference = uri.split("/")[8]

        //  get consultation dce (saved during consultation creation) from businessMappingRepository
        val dce = (businessMappingRepository!!.findByDcIdAndApplicationName(reference, "MS")).block()!!.businessId
        logger.debug("get dce {} ", dce)

        //  soap request and response
        val soapMessage = MSUtils.generateCreateLotLogRequest(login, password, pa, dce, libelle, ordre, numero)
        val response = MSUtils.sendSoap(url, soapMessage)

        //  cleLot parsed from response and saved in businessMapping
        val parseResponse = response.split("&lt;propriete nom=\"cle_lot\"&gt;|&lt;/propriete&gt;".toRegex())
        val cleLot = parseResponse[2]

        val businessMappingLot = BusinessMapping(applicationName = "MSLot", businessId = cleLot, dcId = lot.uuid)
        businessMappingRepository.save(businessMappingLot).block()
        logger.debug("saved businessMapping {} ", businessMappingLot)

        return response
    }

    //TODO: Intégrer dans Receiver
    fun updateLot(login: String, password: String, pa: String, lot: Lot, uri: String, url: String): String {

        //  Lot data formatter
        val uuid = lot.uuid
        val libelle = if (lot.libelle.length > 255) lot.libelle.substring(0, 255) else lot.libelle
        val ordre = lot.ordre.toString()
        val numero = lot.numero.toString()

        //  Get consultation reference from uri
        val reference = uri.split("/")[8]

        //get consultation dce (saved during consultation creation) from businessMappingRepository
        val dce = (businessMappingRepository!!.findByDcIdAndApplicationName(reference, "MS")).block()!!.businessId

        //  get cleLot (saved during lot creation) from businessMappingRepository
        val cleLot = (businessMappingRepository!!.findByDcIdAndApplicationName(uuid, "MSLot")).block()!!.businessId

        //soap request and response
        val soapMessage = MSUtils.generateModifyLotRequest(login, password, pa, dce, cleLot, libelle, ordre, numero)

        return MSUtils.sendSoap(url, soapMessage)
    }

    //TODO: Intégrer dans Receiver
    fun deleteLot(login: String, password: String, pa: String, lot: Lot, uri: String, url: String): String {

        val uuid = lot.uuid

        //  Get consultation reference from uri
        val reference = uri.split("/")[8]

        //  Get consultation dce (saved during consultation creation) from businessMappingRepository
        val dce = (businessMappingRepository!!.findByDcIdAndApplicationName(reference, "MS")).block()!!.businessId

        //  Get cleLot (saved during lot creation) from businessMappingRepository
        val cleLot = (businessMappingRepository!!.findByDcIdAndApplicationName(uuid, "MSLot")).block()!!.businessId

        //SOAP request and response
        val soapMessage = MSUtils.generateDeleteLotRequest(login, password, pa, dce, cleLot)

        return MSUtils.sendSoap(url, soapMessage)
    }

    //TODO: Ou intégrer le service ?
    fun deleteAllLot(login: String, password: String, pa: String, uri: String, url: String): String {

        //  Get consultation reference from uri
        val reference = uri.split("/")[8]

        val dce = (businessMappingRepository!!.findByDcIdAndApplicationName(reference, "MS")).block()!!.businessId

        //  SOAP request and response
        val soapMessage = MSUtils.generateDeleteAllLotRequest(login, password, pa, dce)

        return MSUtils.sendSoap(url, soapMessage)
    }

/*
**     Piece
 */

    fun saveClePiece(response: String, piece: Piece) {
        val piecesList = response.split("&lt;objet type=\"ms_v2__fullweb_piece\"&gt;|&lt;/objet&gt;".toRegex())
        val targetPiece = piecesList.find { s -> s.contains(piece.nom) }
        val parseResponse = targetPiece!!.split("&lt;propriete nom=\"cle_piece\"&gt;|&lt;/propriete&gt;".toRegex())
        if (parseResponse.size >= 2){
            val clePiece = parseResponse[1]
            logger.debug("get clef Pièce {}", clePiece)
            val businessMappingLot = BusinessMapping(applicationName = "MSPiece", businessId = clePiece, dcId = piece.uuid)
            businessMappingRepository.save(businessMappingLot).block()
            logger.debug("saved businessMapping {} ", businessMappingLot)
        }else{
            logger.debug("unable to parse response on clePiece {}", parseResponse)
        }
    }

    fun createPiece(login: String, password: String, pa: String, piece: Piece, uri: String, url: String): String {

        //Piece data formatter
        val libelle = piece.libelle
        val la = MSUtils.booleanToInt(piece.aapc).toString()
        val ordre = piece.ordre.toString()
        val nom = piece.nom
        val extension = piece.extension
        val contenu = Base64.getEncoder().encodeToString(piece.contenu)
        val poids = piece.poids.toString()

        //  Get consultation reference from uri
        val reference = uri.split("/")[8]

        //get cleLot and dce from businessMapping
        val uuidLot = piece.uuidLot!!.substringAfterLast("/")
        var cleLot = ""
        var soapMessage = ""
        try {
            val savedMonoBusinessMapping = businessMappingRepository!!.findByDcIdAndApplicationName(reference, "MS")
            var dce: String = savedMonoBusinessMapping.block()!!.businessId
            if (!uuidLot.isEmpty()) {
                val savedLotMonoBusinessMapping = businessMappingRepository!!.findByDcIdAndApplicationName(uuidLot!!, "MSLot")
                cleLot = savedLotMonoBusinessMapping.block()!!.businessId
            }
            soapMessage = MSUtils.generateCreatePieceLogRequest(login, password, pa, dce, cleLot, libelle, la, ordre, nom, extension, contenu, poids)
            logger.debug("get dce {} and cleLot {}", dce, cleLot)
        } catch (e: IllegalArgumentException) {
            logger.warn("error on finding dce and cleLot from BusinessMapping, ${e.message}")
        }
        val response = MSUtils.sendSoap(url, soapMessage)

        //  clePiece parsed from response and saved in businessMapping
        if(response.contains("objet type=\"error\"".toRegex())){
            logger.error("An error occurs preventing from saving piece in Marche Securise")
        }else{
            saveClePiece(response, piece)
        }
        return response
    }

    fun updatePiece(login: String, password: String, pa: String, piece: Piece, uri: String, url: String): String {

        //Piece data formatter
        val libelle = piece.libelle
        val ordre = piece.ordre.toString()
        val nom = piece.nom
        val extension = piece.extension
        val contenu = Base64.getEncoder().encodeToString(piece.contenu)
        val poids = piece.poids.toString()

        //  Get consultation reference from uri
        val reference = uri.split("/")[8]

        //get cleLot, clePiece and dce from businessMapping
        val uuidLot = piece.uuidLot!!.substringAfterLast("/")
        var soapMessage = ""
        var response = ""
        var cleLot = ""
        try {
            val savedMonoBusinessMapping = businessMappingRepository!!.findByDcIdAndApplicationName(reference, "MS")
            val dce: String = savedMonoBusinessMapping.block()!!.businessId
            if (!uuidLot.isEmpty()) {
                val savedLotMonoBusinessMapping = businessMappingRepository!!.findByDcIdAndApplicationName(uuidLot!!, "MSLot")
                cleLot = savedLotMonoBusinessMapping.block()!!.businessId
            }
            val savedPieceMonoBusinessMapping = businessMappingRepository!!.findByDcIdAndApplicationName(piece.uuid, "MSPiece")
            val clePiece = savedPieceMonoBusinessMapping.block()!!.businessId
            logger.debug("get dce {}, clePiece {} and cleLot {}", dce, clePiece, cleLot)

            soapMessage = MSUtils.generateModifyPieceLogRequest(login, password, pa, dce, clePiece, cleLot, libelle, ordre, nom, extension, contenu, poids)
            response = MSUtils.sendSoap(url, soapMessage)

        } catch (e: Exception) {
            logger.warn("error on finding dce, clePiece or cleLot from BusinessMapping")
            e.printStackTrace()
        }
        return response
    }
}