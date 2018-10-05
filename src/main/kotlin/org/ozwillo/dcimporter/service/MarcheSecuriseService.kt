package org.ozwillo.dcimporter.service

import org.ozwillo.dcimporter.model.BusinessMapping
import org.ozwillo.dcimporter.model.marchepublic.Consultation
import org.ozwillo.dcimporter.model.marchepublic.Lot
import org.ozwillo.dcimporter.model.marchepublic.Piece
import org.ozwillo.dcimporter.repository.BusinessAppConfigurationRepository
import org.ozwillo.dcimporter.repository.BusinessMappingRepository
import org.ozwillo.dcimporter.util.*
import org.ozwillo.dcimporter.util.soap.response.parsing.ResponseType
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.ZoneId
import java.util.*

/*
**      SOAP requests generation and sending to Web Service Marchés Securisés      **
**          * Consultation
**          * Lot
**          * Piece
*/


@Service
class MarcheSecuriseService (private val businessMappingRepository: BusinessMappingRepository,
                             private val businessAppConfigurationRepository: BusinessAppConfigurationRepository) {

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(MarcheSecuriseService::class.java)
        const val name: String = "marche-securise"
    }

    @Value("\${marchesecurise.url.createConsultation}")
    private val createConsultationUrl = ""
    @Value("\${marchesecurise.url.updateConsultation}")
    private val updateConsultationUrl = ""
    @Value("\${marchesecurise.url.deleteConsultation}")
    private val deleteConsultationUrl = ""
    @Value("\${marchesecurise.url.publishConsultation}")
    private val publishConsultationUrl = ""
    @Value("\${marchesecurise.url.lot}")
    private val lotUrl = ""
    @Value("\${marchesecurise.url.piece}")
    private val pieceUrl = ""

/*
**  Consultation **
*/

    private fun sendCreateConsultationRequest(login: String, password: String, pa: String, url: String): String {
        val soapMessage = MSUtils.generateCreateConsultationLogRequest(login, password, pa)
        return MSUtils.sendSoap(url, soapMessage)
    }

    //  Default Consultation creation
    private fun createConsultationAndSaveDce(login: String, password: String, pa: String, consultation: Consultation, uri:String, url: String): ResponseType {
        val response = sendCreateConsultationRequest(login, password, pa, url)
        val responseObject = MSUtils.parseToResponseType(response, MSUtils.CONSULTATION_TYPE, BindingKeyAction.CREATE.value)

        //saving dce (=consultation id in MS)
        val dce = if (responseObject.properties!!.size == 20 && responseObject.properties!![0].status == "changed")responseObject.properties!![0].value else ""
        val msReference = if (responseObject.properties!!.size == 20 && responseObject.properties!![2].name == "reference")responseObject.properties!![2].value!! else ""
        if (!dce!!.isEmpty()){
            val businessMapping = BusinessMapping(applicationName = name, businessId = dce, businessId2 = msReference, dcId = uri, type = MSUtils.CONSULTATION_TYPE)
            businessMappingRepository.save(businessMapping).subscribe()
            logger.debug("saved BusinessMapping : {}", businessMapping)
        }else{
            logger.warn("An error occurred preventing from creating default consultation ${consultation.reference} in Marche Securise")
        }
        return responseObject
    }

    //  Default Consultation creation and update with correct data
    fun createAndUpdateConsultation(siret: String, consultation: Consultation, uri:String): ResponseType {

        //control of already existing businessMapping with same dcId
        val existingBusinessMappings: BusinessMapping? = businessMappingRepository.findByDcIdAndApplicationNameAndType(uri, name, MSUtils.CONSULTATION_TYPE).block()
        val businessAppConfiguration = businessAppConfigurationRepository.findByOrganizationSiretAndApplicationName(siret, name).block()!!
        return if (existingBusinessMappings == null) {
            val creationResponse = createConsultationAndSaveDce(businessAppConfiguration.login!!, businessAppConfiguration.password!!,
                    businessAppConfiguration.instanceId!!, consultation, uri, "${businessAppConfiguration.baseUrl}$createConsultationUrl")
            if(creationResponse.properties!!.size == 20 && creationResponse.properties!![0].status == "changed")
                updateConsultation(siret, consultation, uri)
            else
                creationResponse
        } else {
            logger.warn("Resource with ref '{}' already exists in local database", consultation.reference)
            throw DuplicateError("No consultation creation request sent to Marchés Securisés because resource with ref ${consultation.reference} already exist in local database")
        }
    }


    //  Current consultation updating only
    fun updateConsultation(siret: String, consultation:Consultation, uri:String):ResponseType{

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
        val businessAppConfiguration = businessAppConfigurationRepository.findByOrganizationSiretAndApplicationName(siret, name).block()!!
        var soapMessage = ""
        try {
            val savedMonoBusinessMapping = businessMappingRepository.findByDcIdAndApplicationNameAndType(uri, name, MSUtils.CONSULTATION_TYPE).blockOptional()
            val dce = savedMonoBusinessMapping.map { businessMapping -> businessMapping.businessId }.orElse("")
            soapMessage = MSUtils.generateModifyConsultationLogRequest(businessAppConfiguration.login!!, businessAppConfiguration.password!!, businessAppConfiguration.instanceId!!,
                    dce, objet, enligne, datePublication, dateCloture, reference, finaliteMarche, typeMarche, prestation, passation, informatique, alloti, departement, email)
            logger.debug("get dce {}", dce)
        } catch (e: IllegalArgumentException) {
            logger.warn("error on finding dce from BusinessMapping")
            e.printStackTrace()
        }
        //  SOAP request sending
        var response = ""
        if(!soapMessage.isEmpty()){
            response = MSUtils.sendSoap("${businessAppConfiguration.baseUrl}$updateConsultationUrl", soapMessage)
        }
        return MSUtils.parseToResponseType(response, MSUtils.CONSULTATION_TYPE, BindingKeyAction.UPDATE.value)
    }

    fun deleteConsultation(siret: String, uri:String): ResponseType{

        val soapMessage:String
        var response = ""
        val dce: String

        try {
            val savedMonoBusinessMapping = businessMappingRepository.findByDcIdAndApplicationNameAndType(uri, name, MSUtils.CONSULTATION_TYPE).blockOptional()
            dce = savedMonoBusinessMapping.map { businessMapping -> businessMapping.businessId }.orElse("")
            logger.debug("get dce {}", dce)
            val businessAppConfiguration = businessAppConfigurationRepository.findByOrganizationSiretAndApplicationName(siret, name).block()!!
            soapMessage = MSUtils.generateDeleteConsultationLogRequest(businessAppConfiguration.login!!, businessAppConfiguration.password!!, businessAppConfiguration.instanceId!!, dce)

            //Sending soap request
            if (!soapMessage.isEmpty()){
                response = MSUtils.sendSoap("${businessAppConfiguration.baseUrl}$deleteConsultationUrl", soapMessage)
            }else{
                logger.warn("A problem occurred while generating soap request")
            }
        }catch (e: IllegalArgumentException){
            logger.warn("error on finding dce from BusinessMapping")
            e.printStackTrace()
        }
        //Clean businessMapping
        val responseObject = MSUtils.parseToResponseType(response, MSUtils.CONSULTATION_TYPE, BindingKeyAction.DELETE.value)
        if (responseObject.consultationState == "supprimee"){
            val deletedBusinessMapping = businessMappingRepository.deleteByDcIdAndApplicationNameAndType(uri, name, MSUtils.CONSULTATION_TYPE).subscribe()
            logger.debug("Deletion of businessMapping $deletedBusinessMapping")
        }else{
            logger.warn("Unable to delete consultation $uri")
        }
        return responseObject
    }

    private fun checkConsultationForPublication(dce: String, login:String, password: String, pa: String, baseUrl: String): ResponseType{
        val soapMessage = MSUtils.generateCheckConsultationRequest(login, password, pa, dce)
        var response = ""
        if (!soapMessage.isEmpty()){
            response = MSUtils.sendSoap("$baseUrl$publishConsultationUrl", soapMessage)
        }else{
            logger.warn("A problem occured generating soap request")
        }
        return MSUtils.parseToResponseType(response, MSUtils.CONSULTATION_TYPE, BindingKeyAction.CHECK.value)
    }

    fun publishConsultation(siret: String, uri:String):ResponseType{
        val soapMessage:String
        var response = ""
        val dce: String
        lateinit var checkResponseObject: ResponseType

        try {
            val savedMonoBusinessMapping = businessMappingRepository.findByDcIdAndApplicationNameAndType(uri, name, MSUtils.CONSULTATION_TYPE).blockOptional()
            dce = savedMonoBusinessMapping.map { businessMapping -> businessMapping.businessId }.orElse("")
            logger.debug("get dce {}", dce)

            val businessAppConfiguration = businessAppConfigurationRepository.findByOrganizationSiretAndApplicationName(siret, name).block()!!
            checkResponseObject = checkConsultationForPublication(dce, businessAppConfiguration.login!!, businessAppConfiguration.password!!, businessAppConfiguration.instanceId!!,
                    businessAppConfiguration.baseUrl)
            if (checkResponseObject.properties!![0].value == dce){
                soapMessage = MSUtils.generatePublishConsultationRequest(businessAppConfiguration.login, businessAppConfiguration.password, businessAppConfiguration.instanceId, dce)
                //Sending soap request
                if (!soapMessage.isEmpty()){
                    response = MSUtils.sendSoap("${businessAppConfiguration.baseUrl}$publishConsultationUrl", soapMessage)
                }else{
                    logger.warn("A problem occured generating soap request")
                }
            }else{
                logger.warn("Unable to procced with consultation publication for unknown reasons.")
            }
        }catch (e: IllegalArgumentException){
            logger.warn("error on finding dce from BusinessMapping")
            e.printStackTrace()
        }
        return MSUtils.parseToResponseType(response, MSUtils.CONSULTATION_TYPE, BindingKeyAction.PUBLISH.value)
    }


/*
**  Lot **
*/

    fun saveCleLot(responseObject: ResponseType, uri:String){
        val cleLot: String = if (responseObject.properties != null && responseObject.properties!![0].name == "cle") responseObject.properties!![0].value!! else ""
        if (!cleLot.isEmpty()){
            val savedLotBusinessMapping = businessMappingRepository.findByDcIdAndApplicationNameAndType(uri, name, MSUtils.LOT_TYPE).block()
            if (savedLotBusinessMapping == null){
                val businessMappingLot = BusinessMapping(applicationName = name, businessId = cleLot, dcId = uri, type = MSUtils.LOT_TYPE)
                businessMappingRepository.save(businessMappingLot).subscribe()
                logger.debug("saved businessMapping {} ", businessMappingLot)
            }else{
                throw DuplicateError("Unable to save cleLot because resource with uri $uri already exist in local database")
            }
        }else{
            logger.warn("An error occurred while saving Lot $uri")
        }
    }

    fun createLot(siret: String, lot: Lot, uri: String): ResponseType {

        // Lot data formatter
        val libelle = if (lot.libelle.length > 255) lot.libelle.substring(0, 255) else lot.libelle
        val ordre = lot.ordre.toString()
        val numero = lot.numero.toString()

        //  Get consultation dcId from uri
        val uriConsultation = uri.substringBeforeLast("/").replace(MSUtils.LOT_TYPE, MSUtils.CONSULTATION_TYPE)
        var soapMessage = ""
        var response = ""

        //  get consultation dce (saved during consultation creation) from businessMappingRepository
        val businessAppConfiguration = businessAppConfigurationRepository.findByOrganizationSiretAndApplicationName(siret, name).block()!!
        try {
            val savedMonoBusinessMapping = businessMappingRepository.findByDcIdAndApplicationNameAndType(uriConsultation, name, MSUtils.CONSULTATION_TYPE).blockOptional()
            val dce = savedMonoBusinessMapping.map { businessMapping -> businessMapping.businessId }.orElse("")
            logger.debug("get dce {} ", dce)
            //  SOAP request
            soapMessage = MSUtils.generateCreateLotLogRequest(businessAppConfiguration.login!!, businessAppConfiguration.password!!, businessAppConfiguration.instanceId!!,
                    dce, libelle, ordre, numero)
        }catch (e:IllegalArgumentException){
            logger.warn("error on finding dce from businessMapping, ${e.message}")
        }
        // SOAP sending and response
        if(!soapMessage.isEmpty()){
            response = MSUtils.sendSoap("${businessAppConfiguration.baseUrl}$lotUrl", soapMessage)
        }
        //  cleLot parsed from response and saved in businessMapping
        val responseObject = MSUtils.parseToResponseType(response, MSUtils.LOT_TYPE, BindingKeyAction.CREATE.value, lot.ordre.toString())
        if(responseObject.properties != null && responseObject.properties!!.find { p -> p.value == "error" } == null){
            saveCleLot(responseObject, uri)
        }else{
            logger.error("An error occurs preventing from saving lot in Marche Securise")
        }
        return responseObject
    }

    fun updateLot(siret: String, lot: Lot, uri: String): ResponseType {

        //  Lot data formatter
        val libelle = if (lot.libelle.length > 255) lot.libelle.substring(0, 255) else lot.libelle
        val ordre = lot.ordre.toString()
        val numero = lot.numero.toString()

        //  Get consultation dcId from uri
        val uriConsultation = uri.substringBeforeLast("/").replace(MSUtils.LOT_TYPE, MSUtils.CONSULTATION_TYPE)
        var soapMessage = ""
        var response = ""

        val businessAppConfiguration = businessAppConfigurationRepository.findByOrganizationSiretAndApplicationName(siret, name).block()!!
        try {
            //get consultation dce (saved during consultation creation) from businessMappingRepository
            val savedDceBusinessMapping = businessMappingRepository.findByDcIdAndApplicationNameAndType(uriConsultation, name, MSUtils.CONSULTATION_TYPE).blockOptional()
            val dce = savedDceBusinessMapping.map { businessMapping -> businessMapping.businessId }.orElse("")
            //  get cleLot (saved during lot creation) from businessMappingRepository
            val savedCleLotBusinessMapping = businessMappingRepository.findByDcIdAndApplicationNameAndType(uri, name, MSUtils.LOT_TYPE).blockOptional()
            val cleLot = savedCleLotBusinessMapping.map { businessMapping -> businessMapping.businessId }.orElse("")
            logger.debug("get dce {} and cleLot {} ", dce, cleLot)
            //soap request and response
            soapMessage = MSUtils.generateModifyLotRequest(businessAppConfiguration.login!!, businessAppConfiguration.password!!, businessAppConfiguration.instanceId!!,
                    dce, cleLot, libelle, ordre, numero)
        }catch (e:IllegalArgumentException){
            logger.warn("error on finding dce and cleLot from businessMapping, ${e.message}")
        }
        if(!soapMessage.isEmpty()){
            response = MSUtils.sendSoap("${businessAppConfiguration.baseUrl}$lotUrl", soapMessage)
        }
        return MSUtils.parseToResponseType(response, MSUtils.LOT_TYPE, BindingKeyAction.UPDATE.value, lot.ordre.toString())
    }

    fun deleteLot(siret: String, uri:String): ResponseType {

        //  Get consultation dcId from uri
        val uriConsultation = uri.substringBeforeLast("/").replace(MSUtils.LOT_TYPE, MSUtils.CONSULTATION_TYPE)
        val soapMessage: String
        var response = ""
        lateinit var responseObject: ResponseType

        try {
            //  Get consultation dce (saved during consultation creation) from businessMappingRepository
            val savedDceBusinessMapping = businessMappingRepository.findByDcIdAndApplicationNameAndType(uriConsultation, name, MSUtils.CONSULTATION_TYPE).blockOptional()
            val dce = savedDceBusinessMapping.map { businessMapping -> businessMapping.businessId }.orElse("")
            //  Get cleLot (saved during lot creation) from businessMappingRepository
            val savedCleLotBusinessMapping = businessMappingRepository.findByDcIdAndApplicationNameAndType(uri, name, MSUtils.LOT_TYPE).blockOptional()
            val cleLot = savedCleLotBusinessMapping.map { businessMapping -> businessMapping.businessId }.orElse("")
            logger.debug("get dce {} and cleLot {} ", dce, cleLot)
            val businessAppConfiguration = businessAppConfigurationRepository.findByOrganizationSiretAndApplicationName(siret, name).block()!!
            //SOAP request and response
            soapMessage = MSUtils.generateDeleteLotRequest(businessAppConfiguration.login!!, businessAppConfiguration.password!!, businessAppConfiguration.instanceId!!,
                    dce, cleLot)
            if(!soapMessage.isEmpty()){
                response = MSUtils.sendSoap("${businessAppConfiguration.baseUrl}$lotUrl", soapMessage)
            }
            //Delete businessMapping
            responseObject = MSUtils.parseToResponseType(response, MSUtils.LOT_TYPE, BindingKeyAction.DELETE.value, cleLot)
            if (responseObject.properties != null && responseObject.properties!!.find { p -> p.value == "error" } == null && ((responseObject.properties!!.size >= 5 && responseObject.properties!!.find { p -> p.value == cleLot } == null) || responseObject.properties!![0].value == "supprime")){
                val deletedBusinessMapping = businessMappingRepository.deleteByDcIdAndApplicationNameAndType(uri, name, MSUtils.LOT_TYPE).subscribe()
                logger.debug("deletion of $deletedBusinessMapping")
            }else{
                logger.warn("Unable to delete businessMapping for lot $uri")
            }
        }catch (e:IllegalArgumentException){
            logger.warn("error on finding dce and cleLot from businessMapping, ${e.message}")
        }
        return responseObject
    }

    //TODO: Ou intégrer le service ?
    fun deleteAllLot(siret: String, uri: String, url: String): String {

        val savedDceBusinessMapping = businessMappingRepository.findByDcIdAndApplicationNameAndType(uri, name, MSUtils.CONSULTATION_TYPE).blockOptional()
        val dce = savedDceBusinessMapping.map { businessMapping -> businessMapping.businessId }.orElse("")
        val businessAppConfiguration = businessAppConfigurationRepository.findByOrganizationSiretAndApplicationName(siret, name).block()!!

        //  SOAP request and response
        val soapMessage = MSUtils.generateDeleteAllLotRequest(businessAppConfiguration.login!!, businessAppConfiguration.password!!, businessAppConfiguration.instanceId!!, dce)

        return MSUtils.sendSoap("${businessAppConfiguration.baseUrl}$url", soapMessage)
    }

/*
**     Piece
 */

    fun saveClePiece(responseObject: ResponseType, uri:String) {
        val clePiece = if (responseObject.properties != null && responseObject.properties!![0].name == "cle_piece") responseObject.properties!![0].value!! else ""
        if (!clePiece.isEmpty()){
            val savedPieceBusinessMapping = businessMappingRepository.findByDcIdAndApplicationNameAndType(uri, name, MSUtils.PIECE_TYPE).block()
            if (savedPieceBusinessMapping == null){
                val businessMappingLot = BusinessMapping(applicationName = name, businessId = clePiece, dcId = uri, type = MSUtils.PIECE_TYPE)
                businessMappingRepository.save(businessMappingLot).subscribe()
                logger.debug("saved businessMapping {} ", businessMappingLot)
            }else{
                logger.warn("Unable to save clePiece because resource with uri $uri already exist")
            }
        }else{
            logger.warn("An error occurred while saving Piece $uri")
        }
    }

    fun createPiece(siret: String, piece: Piece, uri: String): ResponseType {

        //Piece data formatter
        val libelle = piece.libelle
        val la = MSUtils.booleanToInt(piece.aapc).toString()
        val ordre = piece.ordre.toString()
        val nom = piece.nom
        val extension = piece.extension
        val contenu = Base64.getEncoder().encodeToString(piece.contenu)
        val poids = piece.poids.toString()

        var response =""
        lateinit var responseObject: ResponseType
        if (MSUtils.convertOctetToMo(piece.poids) <= 7.14) {
            //  Get consultation dcId from uri
            val uriConsultation = uri.substringBeforeLast("/").replace(MSUtils.PIECE_TYPE, MSUtils.CONSULTATION_TYPE)

            //get cleLot and dce from businessMapping
            val uuidLot = piece.uuidLot ?: ""
            var cleLot = ""
            var soapMessage = ""
            val businessAppConfiguration = businessAppConfigurationRepository.findByOrganizationSiretAndApplicationName(siret, name).block()!!
            try {
                val savedMonoBusinessMapping = businessMappingRepository.findByDcIdAndApplicationNameAndType(uriConsultation, name, MSUtils.CONSULTATION_TYPE).blockOptional()
                val dce = savedMonoBusinessMapping.map { businessMapping -> businessMapping.businessId }.orElse("")
                if (!uuidLot.isEmpty()) {
                    val savedLotMonoBusinessMapping = businessMappingRepository.findByDcIdAndApplicationNameAndType(uuidLot, name, MSUtils.LOT_TYPE).blockOptional()
                    cleLot = savedLotMonoBusinessMapping.map { businessMapping -> businessMapping.businessId }.orElse("")
                }
                soapMessage = MSUtils.generateCreatePieceLogRequest(businessAppConfiguration.login!!, businessAppConfiguration.password!!, businessAppConfiguration.instanceId!!, dce, cleLot, libelle, la, ordre, nom, extension, contenu, poids)
                logger.debug("get dce {} and cleLot {}", dce, cleLot)
            } catch (e: IllegalArgumentException) {
                logger.warn("error on finding dce and cleLot from BusinessMapping, ${e.message}")
            }
            if (!soapMessage.isEmpty()){
                response = MSUtils.sendSoap("${businessAppConfiguration.baseUrl}$pieceUrl", soapMessage)
            }

            //  clePiece parsed from response and saved in businessMapping
            responseObject = MSUtils.parseToResponseType(response, MSUtils.PIECE_TYPE, BindingKeyAction.CREATE.value, "${piece.nom}.${piece.extension}")
            if(responseObject.properties != null && responseObject.type != "error" && responseObject.properties!!.size >= 5 && responseObject.properties!![5].value == "${piece.nom}.${piece.extension}"){
                saveClePiece(responseObject, uri)
            }else{
                logger.error("An error occurs preventing from saving piece in Marche Securise")
            }
        } else {
            throw PieceSizeError("File size ${piece.poids} exceeds allowed size limit of 7486832.64 octet, please delete uri $uri and retry with a correct file.")
        }
        return responseObject
    }

    fun updatePiece(siret: String, piece: Piece, uri: String, url: String): String {

        //Piece data formatter
        val libelle = piece.libelle
        val ordre = piece.ordre.toString()
        val nom = piece.nom
        val extension = piece.extension
        val contenu = Base64.getEncoder().encodeToString(piece.contenu)
        val poids = piece.poids.toString()

        //  Get dcId reference from uri
        val uriConsultation = uri.substringBeforeLast("/").replace(MSUtils.PIECE_TYPE, MSUtils.CONSULTATION_TYPE)

        //get cleLot, clePiece and dce from businessMapping
        val uuidLot = piece.uuidLot ?: ""
        var soapMessage = ""
        var response = ""
        var cleLot = ""
        val businessAppConfiguration = businessAppConfigurationRepository.findByOrganizationSiretAndApplicationName(siret, name).block()!!
        try {
            val savedMonoBusinessMapping = businessMappingRepository.findByDcIdAndApplicationNameAndType(uriConsultation, name, MSUtils.CONSULTATION_TYPE).blockOptional()
            val dce = savedMonoBusinessMapping.map { businessMapping -> businessMapping.businessId }.orElse("")
            if (!uuidLot.isEmpty()) {
                val savedLotMonoBusinessMapping = businessMappingRepository.findByDcIdAndApplicationNameAndType(uuidLot, name, MSUtils.LOT_TYPE).blockOptional()
                cleLot = savedLotMonoBusinessMapping.map { businessMapping -> businessMapping.businessId }.orElse("")
            }
            val savedPieceMonoBusinessMapping = businessMappingRepository.findByDcIdAndApplicationNameAndType(uri, name, MSUtils.PIECE_TYPE).blockOptional()
            val clePiece = savedPieceMonoBusinessMapping.map { businessMapping -> businessMapping.businessId }.orElse("")
            logger.debug("get dce {}, clePiece {} and cleLot {}", dce, clePiece, cleLot)

            soapMessage = MSUtils.generateModifyPieceLogRequest(businessAppConfiguration.login!!, businessAppConfiguration.password!!, businessAppConfiguration.instanceId!!, dce, clePiece, cleLot, libelle, ordre, nom, extension, contenu, poids)
        } catch (e: IllegalArgumentException) {
            logger.warn("error on finding dce, clePiece or cleLot from BusinessMapping, ${e.message}")
        }
        if(!soapMessage.isEmpty()){
            response = MSUtils.sendSoap("${businessAppConfiguration.baseUrl}$url", soapMessage)
        }
        return response
    }

    fun deletePiece(siret: String, uri: String):ResponseType{

        //Get consultation dcId from iri
        val uriConsultation = uri.substringBeforeLast("/").replace(MSUtils.PIECE_TYPE, MSUtils.CONSULTATION_TYPE)
        val soapMessage:String
        var response = ""
        lateinit var responseObject: ResponseType

        try {
            //Get consultation dce from businessMapping
            val savedMonoBusinessMapping = businessMappingRepository.findByDcIdAndApplicationNameAndType(uriConsultation, name, MSUtils.CONSULTATION_TYPE).blockOptional()
            val dce = savedMonoBusinessMapping.map { businessMapping -> businessMapping.businessId }.orElse("")
            //Get piece clePiece from businessMapping
            val savedPieceMonoBusinessMapping = businessMappingRepository.findByDcIdAndApplicationNameAndType(uri, name, MSUtils.PIECE_TYPE).blockOptional()
            val clePiece = savedPieceMonoBusinessMapping.map { businessMapping -> businessMapping.businessId }.orElse("")
            logger.debug("get dce {} and clePiece {}", dce, clePiece)
            val businessAppConfiguration = businessAppConfigurationRepository.findByOrganizationSiretAndApplicationName(siret, name).block()!!
            soapMessage = MSUtils.generateDeletePieceRequest(businessAppConfiguration.login!!, businessAppConfiguration.password!!, businessAppConfiguration.instanceId!!, dce, clePiece)
            if (!soapMessage.isEmpty()){
                response = MSUtils.sendSoap("${businessAppConfiguration.baseUrl}$pieceUrl", soapMessage)
            }
            responseObject = MSUtils.parseToResponseType(response, MSUtils.PIECE_TYPE, BindingKeyAction.DELETE.value, clePiece)
            if (responseObject.properties != null && responseObject.properties!!.find { p -> p.value == clePiece } == null && (responseObject.properties!![0].name == "cle_piece" || responseObject.properties!![0].value == "supprime")){
                //Delete businessMapping
                val deletedBusinessMapping = businessMappingRepository.deleteByDcIdAndApplicationNameAndType(uri, name, MSUtils.PIECE_TYPE).subscribe()
                logger.debug("deletion of $deletedBusinessMapping")
            }else{
                logger.warn("Unable to delete piece $uri from businessMapping")
            }
        }catch (e:IllegalArgumentException){
            logger.warn("error on finding dce and clePiece from businessMapping, ${e.message}")
        }
        return responseObject
    }
}