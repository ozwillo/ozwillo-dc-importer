package org.ozwillo.dcimporter.service

import org.ozwillo.dcimporter.model.BusinessMapping
import org.ozwillo.dcimporter.model.marchepublic.Consultation
import org.ozwillo.dcimporter.model.marchepublic.Lot
import org.ozwillo.dcimporter.model.marchepublic.Piece
import org.ozwillo.dcimporter.repository.BusinessMappingRepository
import org.ozwillo.dcimporter.util.MSUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
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
class MarcheSecuriseService (private val businessMappingRepository: BusinessMappingRepository) {

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(MarcheSecuriseService::class.java)
    }

    private val CONSULTATION_TYPE = "marchepublic:consultation_0"
    private val LOT_TYPE = "marchepublic:lot_0"
    private val PIECE_TYPE = "marchepublic:piece_0"

    @Value("\${marchesecurise.url.updateConsultation}")
    private val updateConsultationUrl = ""

/*
**  Consultation **
*/

    private fun sendCreateConsultationRequest(login: String, password: String, pa: String, url: String): String {
        val soapMessage = MSUtils.generateCreateConsultationLogRequest(login, password, pa)
        return MSUtils.sendSoap(url, soapMessage)
    }

    //  Default Consultation creation
    private fun createConsultationAndSaveDce(login: String, password: String, pa: String, consultation: Consultation, uri:String, url: String): String {
        val response = sendCreateConsultationRequest(login, password, pa, url)

        //saving dce (=consultation id in MS)
        if (response.contains("<propriete nom=\"cle\" statut=\"changed\">")){
            val parseResponse: List<String> = response.split("<propriete nom=\"cle\" statut=\"changed\">|</propriete>".toRegex())
            val dce = parseResponse[1]
            val businessMapping = BusinessMapping(applicationName = "MS", businessId = dce, dcId = uri, type = CONSULTATION_TYPE)
            logger.debug("saved BusinessMapping : {}", businessMapping)
            businessMappingRepository.save(businessMapping).block()
        }else{
            logger.warn("An error occurred preventing from creating default consultation ${consultation.reference} in Marche Securise")
        }
        return response
    }

    //  Default Consultation creation and update with correct data
    fun createAndUpdateConsultation(login: String, password: String, pa: String, consultation: Consultation, uri:String, url: String): String {

        var response:String

        //control of already existing businessMapping with same dcId
        val existingBusinessMappings: BusinessMapping? = businessMappingRepository.findByDcIdAndApplicationNameAndType(uri, "MS", CONSULTATION_TYPE).block()

        response = if (existingBusinessMappings == null) {
            val creationResponse = createConsultationAndSaveDce(login, password, pa, consultation, uri, url)
            if(creationResponse.contains("<propriete nom=\"cle\" statut=\"changed\">")) updateConsultation(login, password, pa, consultation, uri, updateConsultationUrl) else "An error occurs during consultation creation"
        } else {
            logger.warn("Resource with ref '{}' already exists", consultation.reference)
            "No consultation creation request sent to Marche Securise"
        }
        return response
    }


    //  Current consultation updating only
    fun updateConsultation(login:String, password:String, pa:String, consultation:Consultation, uri:String, url: String):String{

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
        val informatique = MSUtils.booleanToInt(consultation.informatique).toString()
        val alloti = MSUtils.booleanToInt(consultation.alloti).toString()
        val departement = MSUtils.intListToString(consultation.departementsPrestation)
        val email = if ((MSUtils.stringListToString(consultation.emails)).length > 255) (MSUtils.stringListToString(consultation.emails)).substring(0, 255) else MSUtils.stringListToString(consultation.emails)

        //  get consultation dce from BusinessMappingRepository and generate SOAP request
        var soapMessage = ""
        try {
            val savedMonoBusinessMapping = businessMappingRepository.findByDcIdAndApplicationNameAndType(uri, "MS", CONSULTATION_TYPE)
            val dce: String = savedMonoBusinessMapping.block()!!.businessId
            soapMessage = MSUtils.generateModifyConsultationLogRequest(login, password, pa, dce, objet, enligne, datePublication, dateCloture, reference, finaliteMarche, typeMarche, prestation, passation, informatique, alloti, departement, email)
            logger.debug("get dce {}", dce)
        } catch (e: Exception) {
            logger.warn("error on finding dce from BusinessMapping")
            e.printStackTrace()
        }
        //  SOAP request sending
        var response = ""
        if(!soapMessage.isEmpty()){
            response = MSUtils.sendSoap(url, soapMessage)
        }
        return response
    }

    fun deleteConsultation(login: String, password: String, pa: String, uri:String, url: String):String{

        var soapMessage:String
        var response = ""

        try {
            val savedMonoBusinessMapping = businessMappingRepository.findByDcIdAndApplicationNameAndType(uri, "MS", CONSULTATION_TYPE)
            val dce: String = savedMonoBusinessMapping.block()!!.businessId
            logger.debug("get dce {}", dce)
            soapMessage = MSUtils.generateDeleteConsultationLogRequest(login, password, pa, dce)

            //Sending soap request
            if (!soapMessage.isEmpty()){
                response = MSUtils.sendSoap(url, soapMessage)
            }else{
                logger.warn("A problem occurred while generating soap request")
            }
            //Clean businessMapping
            if (response.contains("suppression_consultation_ok")){
                val deletedBusinessMapping = businessMappingRepository.deleteByDcIdAndApplicationNameAndType(uri, "MS", CONSULTATION_TYPE).subscribe()
                logger.debug("Deletion of businessMapping $deletedBusinessMapping")
            }else{
                logger.warn("Unable to delete consultation $uri")
            }
        } catch (e: Exception) {
            logger.warn("error on finding dce from BusinessMapping")
            e.printStackTrace()
        }

        return response
    }

    fun publishConsultation(login: String, password: String, pa: String, uri:String, url: String):String{

        var soapMessage:String
        var response = ""

        try {
            val savedMonoBusinessMapping = businessMappingRepository.findByDcIdAndApplicationName(uri, "MS")
            val dce: String = savedMonoBusinessMapping.block()!!.businessId
            logger.debug("get dce {}", dce)

            soapMessage = MSUtils.generateCheckConsultationRequest(login, password, pa, dce)
            if (!soapMessage.isEmpty()){
                response = MSUtils.sendSoap(url, soapMessage)
            }else{
                logger.warn("A problem occured generating soap request")
            }
            if (response.contains("<objet type=\"ms_v2__fullweb_dce\">")){
                soapMessage = MSUtils.generatePublishConsultationRequest(login, password, pa, dce)
                //Sending soap request
                if (!soapMessage.isEmpty()){
                    response = MSUtils.sendSoap(url, soapMessage)
                }else{
                    logger.warn("A problem occured generating soap request")
                }
            }else if (response.contains("validation_erreur")){
                val error = (response.split("<validation_erreur erreur_0=|cle=".toRegex()))[1]
                logger.warn("Unable to procced with consultation publication because of following error : $error")
            }else{
                logger.warn("Unable to procced with consultation publication for unknown reasons. Soap response : $response")
            }
        } catch (e: Exception) {
            logger.warn("error on finding dce from BusinessMapping")
            e.printStackTrace()
        }
        return response
    }


/*
**  Lot **
*/

    fun saveCleLot(response: String, lot: Lot, uri:String){
        if (response.contains("<objet type=\"ms_v2__fullweb_lot\">")){
            val lotList = response.split("<objet type=\"ms_v2__fullweb_lot\">|</objet>".toRegex())
            if(response.contains("<propriete nom=\"cle_lot\">")){
                val targetLot = lotList.find { s -> s.contains("<propriete nom=\"ordre\">${lot.ordre}</propriete>")}
                val parseResponse = targetLot!!.split("<propriete nom=\"cle_lot\">|</propriete>".toRegex())
                if(parseResponse.size >= 3){
                    val cleLot = parseResponse[2]
                    val businessMappingLot = BusinessMapping(applicationName = "MS", businessId = cleLot, dcId = uri, type = LOT_TYPE)
                    businessMappingRepository.save(businessMappingLot).block()
                    logger.debug("saved businessMapping {} ", businessMappingLot)
                }else{
                    logger.warn("unable to parse response in cleLot for response {}", parseResponse)
                }
            }else{
                logger.warn("An error occurred while saving Lot ${lot.libelle}")
            }
        }else{
            logger.warn("An error occurred while saving Lot ${lot.libelle}")
        }
    }

    fun createLot(login: String, password: String, pa: String, lot: Lot, uri: String, url: String): String {

        // Lot data formatter
        val libelle = if (lot.libelle.length > 255) lot.libelle.substring(0, 255) else lot.libelle
        val ordre = lot.ordre.toString()
        val numero = lot.numero.toString()

        //  Get consultation dcId from uri
        val uriConsultation = uri.substringBeforeLast("/").replace(LOT_TYPE, CONSULTATION_TYPE)
        var soapMessage = ""
        var response = ""

        //  get consultation dce (saved during consultation creation) from businessMappingRepository
        try {
            val savedMonoBusinessMapping = businessMappingRepository.findByDcIdAndApplicationNameAndType(uriConsultation, "MS", CONSULTATION_TYPE)
            val dce = savedMonoBusinessMapping.block()!!.businessId
            logger.debug("get dce {} ", dce)
            //  SOAP request
            soapMessage = MSUtils.generateCreateLotLogRequest(login, password, pa, dce, libelle, ordre, numero)
        }catch (e:IllegalArgumentException){
            logger.warn("error on finding dce from businessMapping, ${e.message}")
        }
        // SOAP sending and response
        if(!soapMessage.isEmpty()){
            response = MSUtils.sendSoap(url, soapMessage)
        }
        //  cleLot parsed from response and saved in businessMapping
        if(!response.contains("objet type=\"error\"".toRegex()) || response.contains("SOAP-ERROR")){
            saveCleLot(response, lot, uri)
        }else{
            logger.error("An error occurs preventing from saving lot in Marche Securise")
        }
        return response
    }

    fun updateLot(login: String, password: String, pa: String, lot: Lot, uri: String, url: String): String {

        //  Lot data formatter
        val libelle = if (lot.libelle.length > 255) lot.libelle.substring(0, 255) else lot.libelle
        val ordre = lot.ordre.toString()
        val numero = lot.numero.toString()

        //  Get consultation dcId from uri
        val uriConsultation = uri.substringBeforeLast("/").replace(LOT_TYPE, CONSULTATION_TYPE)
        var soapMessage = ""
        var response = ""

        try {
            //get consultation dce (saved during consultation creation) from businessMappingRepository
            val dce = (businessMappingRepository.findByDcIdAndApplicationNameAndType(uriConsultation, "MS", CONSULTATION_TYPE)).block()!!.businessId
            //  get cleLot (saved during lot creation) from businessMappingRepository
            val cleLot = (businessMappingRepository.findByDcIdAndApplicationNameAndType(uri, "MS", LOT_TYPE)).block()!!.businessId
            logger.debug("get dce {} and cleLot {} ", dce, cleLot)
            //soap request and response
            soapMessage = MSUtils.generateModifyLotRequest(login, password, pa, dce, cleLot, libelle, ordre, numero)
        }catch (e:IllegalArgumentException){
            logger.warn("error on finding dce and cleLot from businessMapping, ${e.message}")
        }
        if(!soapMessage.isEmpty()){
            response = MSUtils.sendSoap(url, soapMessage)
        }
        return response
    }

    fun deleteLot(login: String, password: String, pa: String, uri:String, url: String): String {

        //  Get consultation dcId from uri
        val uriConsultation = uri.substringBeforeLast("/").replace(LOT_TYPE, CONSULTATION_TYPE)
        var soapMessage: String
        var response = ""

        try {
            //  Get consultation dce (saved during consultation creation) from businessMappingRepository
            val dce = (businessMappingRepository.findByDcIdAndApplicationNameAndType(uriConsultation, "MS", CONSULTATION_TYPE)).block()!!.businessId
            //  Get cleLot (saved during lot creation) from businessMappingRepository
            val cleLot = (businessMappingRepository.findByDcIdAndApplicationNameAndType(uri, "MS", LOT_TYPE)).block()!!.businessId
            logger.debug("get dce {} and cleLot {} ", dce, cleLot)
            //SOAP request and response
            soapMessage = MSUtils.generateDeleteLotRequest(login, password, pa, dce, cleLot)
            if(!soapMessage.isEmpty()){
                response = MSUtils.sendSoap(url, soapMessage)
            }
            //Delete businessMapping
            if (response.contains("<objet type=\"ms_v2__fullweb_lot\">")){
                val deletedBusinessMapping = businessMappingRepository.deleteByDcIdAndApplicationNameAndType(uri, "MS", LOT_TYPE).subscribe()
                logger.debug("deletion of $deletedBusinessMapping")
            }else{
                logger.warn("Unable to delete businessMapping for lot $uri")
            }
        }catch (e:IllegalArgumentException){
            logger.warn("error on finding dce and cleLot from businessMapping, ${e.message}")
        }
        return response
    }

    //TODO: Ou intégrer le service ?
    fun deleteAllLot(login: String, password: String, pa: String, uri: String, url: String): String {

        //  Get consultation dcId from uri
        val reference = uri.split("/")[8]

        val dce = (businessMappingRepository.findByDcIdAndApplicationName(reference, "MS")).block()!!.businessId

        //  SOAP request and response
        val soapMessage = MSUtils.generateDeleteAllLotRequest(login, password, pa, dce)

        return MSUtils.sendSoap(url, soapMessage)
    }

/*
**     Piece
 */

    fun saveClePiece(response: String, piece: Piece, uri:String) {
        if (response.contains("<objet type=\"ms_v2__fullweb_piece\">")){
            val piecesList = response.split("<objet type=\"ms_v2__fullweb_piece\">|</objet>".toRegex())
            if (response.contains("<propriete nom=\"nom\">")){
                val targetPiece = piecesList.find { s -> s.contains("<propriete nom=\"nom\">${piece.nom}.txt</propriete>") }
                val parseResponse = targetPiece!!.split("<propriete nom=\"cle_piece\">|</propriete>".toRegex())
                if (parseResponse.size >= 2){
                    val clePiece = parseResponse[1]
                    logger.debug("get clef Pièce {}", clePiece)
                    val businessMappingLot = BusinessMapping(applicationName = "MS", businessId = clePiece, dcId = uri, type = PIECE_TYPE)
                    businessMappingRepository.save(businessMappingLot).block()
                    logger.debug("saved businessMapping {} ", businessMappingLot)
                }else{
                    logger.debug("unable to parse response on clePiece {}", parseResponse)
                }
            }else{
                logger.warn("An error occurred while saving Piece ${piece.libelle}")
            }
        }else{
            logger.warn("An error occurred while saving Piece ${piece.libelle}")
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

        var response =""
        if (MSUtils.convertOctetToMo(piece.poids) <= 7.14) {
            //  Get consultation dcId from uri
            val uriConsultation = uri.substringBeforeLast("/").replace(PIECE_TYPE, CONSULTATION_TYPE)

            //get cleLot and dce from businessMapping
            val uuidLot = piece.uuidLot ?: ""
            var cleLot = ""
            var soapMessage = ""
            try {
                val savedMonoBusinessMapping = businessMappingRepository.findByDcIdAndApplicationNameAndType(uriConsultation, "MS", CONSULTATION_TYPE)
                var dce: String = savedMonoBusinessMapping.block()!!.businessId
                if (!uuidLot.isEmpty()) {
                    val savedLotMonoBusinessMapping = businessMappingRepository.findByDcIdAndApplicationNameAndType(uuidLot, "MS", LOT_TYPE)
                    cleLot = savedLotMonoBusinessMapping.block()!!.businessId
                }
                soapMessage = MSUtils.generateCreatePieceLogRequest(login, password, pa, dce, cleLot, libelle, la, ordre, nom, extension, contenu, poids)
                logger.debug("get dce {} and cleLot {}", dce, cleLot)
            } catch (e: IllegalArgumentException) {
                logger.warn("error on finding dce and cleLot from BusinessMapping, ${e.message}")
            }
            if (!soapMessage.isEmpty()){
                response = MSUtils.sendSoap(url, soapMessage)
            }

            //  clePiece parsed from response and saved in businessMapping
            if(!response.contains("objet type=\"error\"".toRegex())){
                saveClePiece(response, piece, uri)
            }else{
                logger.error("An error occurs preventing from saving piece in Marche Securise")
            }
        } else {
            logger.error("File size ${piece.poids} exceeds allowed size limit of 7486832.64 octet")
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

        //  Get dcId reference from uri
        val uriConsultation = uri.substringBeforeLast("/").replace(PIECE_TYPE, CONSULTATION_TYPE)

        //get cleLot, clePiece and dce from businessMapping
        val uuidLot = piece.uuidLot ?: ""
        var soapMessage = ""
        var response = ""
        var cleLot = ""
        try {
            val savedMonoBusinessMapping = businessMappingRepository.findByDcIdAndApplicationNameAndType(uriConsultation, "MS", CONSULTATION_TYPE)
            val dce: String = savedMonoBusinessMapping.block()!!.businessId
            if (!uuidLot.isEmpty()) {
                val savedLotMonoBusinessMapping = businessMappingRepository.findByDcIdAndApplicationNameAndType(uuidLot, "MS", LOT_TYPE)
                cleLot = savedLotMonoBusinessMapping.block()!!.businessId
            }
            val savedPieceMonoBusinessMapping = businessMappingRepository.findByDcIdAndApplicationNameAndType(uri, "MS", PIECE_TYPE)
            val clePiece = savedPieceMonoBusinessMapping.block()!!.businessId
            logger.debug("get dce {}, clePiece {} and cleLot {}", dce, clePiece, cleLot)

            soapMessage = MSUtils.generateModifyPieceLogRequest(login, password, pa, dce, clePiece, cleLot, libelle, ordre, nom, extension, contenu, poids)
        } catch (e: IllegalArgumentException) {
            logger.warn("error on finding dce, clePiece or cleLot from BusinessMapping, ${e.message}")
        }
        if(!soapMessage.isEmpty()){
            response = MSUtils.sendSoap(url, soapMessage)
        }
        return response
    }

    fun deletePiece(login: String, password: String, pa: String, uri: String, url: String):String{

        //Get consultation dcId from iri
        val uriConsultation = uri.substringBeforeLast("/").replace(PIECE_TYPE, CONSULTATION_TYPE)
        var soapMessage:String
        var response = ""

        try {
            //Get consultation dce from businessMapping
            val savedMonoBusinessMapping = businessMappingRepository.findByDcIdAndApplicationNameAndType(uriConsultation, "MS", CONSULTATION_TYPE)
            val dce = savedMonoBusinessMapping.block()!!.businessId
            //Get piece clePiece from businessMapping
            val savedPieceMonoBusinessMapping = businessMappingRepository.findByDcIdAndApplicationNameAndType(uri, "MS", PIECE_TYPE)
            val clePiece = savedPieceMonoBusinessMapping.block()!!.businessId
            logger.debug("get dce {} and clePiece {}", dce, clePiece)
            soapMessage = MSUtils.generateDeletePieceRequest(login, password, pa, dce, clePiece)
            if (!soapMessage.isEmpty()){
                response = MSUtils.sendSoap(url, soapMessage)
            }
            if (response.contains("<objet type=\"ms_v2__fullweb_piece\">")){
                //Delete businessMapping
                val deletedBusinessMapping = businessMappingRepository.deleteByDcIdAndApplicationNameAndType(uri, "MS", PIECE_TYPE).subscribe()
                logger.debug("deletion of $deletedBusinessMapping")
            }else{
                logger.warn("Unable to delete piece $uri")
            }
        }catch (e:IllegalArgumentException){
            logger.warn("error on finding dce and clePiece from businessMapping, ${e.message}")
        }
        return response
    }
}