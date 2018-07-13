package org.ozwillo.dcimporter.service.marchesecurise

import org.ozwillo.dcimporter.model.BusinessMapping
import org.ozwillo.dcimporter.model.marchepublic.Consultation
import org.ozwillo.dcimporter.model.marchepublic.Lot
import org.ozwillo.dcimporter.repository.BusinessMappingRepository
import org.ozwillo.dcimporter.service.marchesecurise.rabbitMQ.ReceiverMS
import org.ozwillo.dcimporter.util.DCUtils
import org.ozwillo.dcimporter.util.MSUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.sql.Timestamp

/*
**      SOAP requests generation and sending to Web Service Marche Securise      **
**          * Consultation
**          * Lot
**          * Piece
*/


@Service
class MarcheSecuriseService{

    companion object {
        private val LOGGER: Logger = LoggerFactory.getLogger(ReceiverMS::class.java)
    }

    @Value("\${marchesecurise.config.url.updateConsultation}")
    private val UPDATE_CONSULTATION_URL = ""

/*
**  Consultation **
*/

    private fun sendCreateConsultationRequest (login:String, password:String, pa:String, url:String):String{
        val soapMessage = MSUtils.generateCreateConsultationLogRequest(login, password,pa)
        return MSUtils.sendSoap(url, soapMessage)
    }

    //  Default Consultation creation
    private fun createConsultationAndSaveDce(login:String, password:String, pa:String, url:String, consultation: Consultation, businessMappingRepository: BusinessMappingRepository):String {
        val response = sendCreateConsultationRequest(login, password, pa, url)
        val reference: String = consultation.reference!!

        //saving dce (=consultation id in MS)
        val parseResponse: List<String> = response.split("&lt;propriete nom=\"cle\" statut=\"changed\"&gt;|&lt;/propriete&gt;".toRegex())
        val dce = parseResponse[1]
        val businessMapping = BusinessMapping(applicationName = "MS", businessId = dce, dcId = reference)
        LOGGER.debug("saved BusinessMapping : {}", businessMapping)
        businessMappingRepository!!.save(businessMapping).block()

        return response
    }

    //  Default Consultation creation and update with correct data
    fun createAndUpdateConsultation(login:String, password:String, pa:String, consultation: Consultation, url:String, businessMappingRepository: BusinessMappingRepository):String{

        //  consultation data formatter
        val objet = if((consultation.objet).length > 255) (consultation.objet).substring(0,255) else consultation.objet
        val enligne = DCUtils.booleanToInt(consultation.enLigne).toString()
        val datePublication = ((Timestamp.valueOf(consultation.datePublication).time)/1000).toString()
        val dateCloture = ((Timestamp.valueOf(consultation.dateCloture).time)/1000).toString()
        val reference = consultation.reference.toString()
        val finaliteMarche = (consultation.finaliteMarche).toString().toLowerCase()
        val typeMarche = (consultation.typeMarche).toString().toLowerCase()
        val prestation = (consultation.typePrestation).toString().toLowerCase()
        val passation = consultation.passation
        val alloti = DCUtils.booleanToInt(consultation.alloti).toString()
        val departement = DCUtils.intListToString(consultation.departementsPrestation)
        val email = if((DCUtils.stringListToString(consultation.emails)).length > 255) (DCUtils.stringListToString(consultation.emails)).substring(0,255) else DCUtils.stringListToString(consultation.emails)


        createConsultationAndSaveDce(login, password, pa, url, consultation, businessMappingRepository)

        //  get consultation dce from BusinessMappingRepository and generate SOAP request
        var soapMessage = ""
        try {
            val savedMonoBusinessMapping = businessMappingRepository!!.findFirstByDcIdAndApplicationName(reference, "MS")
            var dce: String = savedMonoBusinessMapping.block()!!.businessId
            soapMessage = MSUtils.generateModifyConsultationLogRequest(login, password, pa, dce, objet, enligne, datePublication, dateCloture, reference, finaliteMarche, typeMarche, prestation, passation, alloti, departement, email)


            LOGGER.debug("get dce {}", dce)
        } catch (e: Exception) {
            LOGGER.error("error on finding dce from BusinessMapping")
            e.printStackTrace()
        }

        //  SOAP request sending
        return MSUtils.sendSoap(UPDATE_CONSULTATION_URL, soapMessage)
    }


    //  Current consultation updating only
    //TODO:Intégrer la fonction dans ReceiverMS + sender dans DataCoreService update ()
    fun updateConsultation(login:String, password:String, pa:String, consultation:Consultation, url: String, businessMappingRepository: BusinessMappingRepository):String{

        //  Consultation data formatter
        val objet = if((consultation.objet).length > 255) (consultation.objet).substring(0,255) else consultation.objet
        val enligne = DCUtils.booleanToInt(consultation.enLigne).toString()
        val datePublication = ((Timestamp.valueOf(consultation.datePublication).time)/1000).toString()
        val dateCloture = ((Timestamp.valueOf(consultation.dateCloture).time)/1000).toString()
        val reference = consultation.reference.toString()
        val finaliteMarche = (consultation.finaliteMarche).toString().toLowerCase()
        val typeMarche = (consultation.typeMarche).toString().toLowerCase()
        val prestation = (consultation.typePrestation).toString().toLowerCase()
        val passation = consultation.passation
        val alloti = DCUtils.booleanToInt(consultation.alloti).toString()
        val departement = DCUtils.intListToString(consultation.departementsPrestation)
        val email = if((DCUtils.stringListToString(consultation.emails)).length > 255) (DCUtils.stringListToString(consultation.emails)).substring(0,255) else DCUtils.stringListToString(consultation.emails)
        val save = businessMappingRepository!!.findByDcIdAndApplicationName(reference, "MS")
        val dce = (save).block()!!.businessId

        //  get consultation dce from BusinessMappingRepository and generate SOAP request
        var soapMessage = ""
        try {
            val savedMonoBusinessMapping = businessMappingRepository.findFirstByDcIdAndApplicationName(reference, "MS")
            var dce: String = savedMonoBusinessMapping.block()!!.businessId
            soapMessage = MSUtils.generateModifyConsultationLogRequest(login, password, pa, dce, objet, enligne, datePublication, dateCloture, reference, finaliteMarche, typeMarche, prestation, passation, alloti, departement, email)


            LOGGER.debug("get dce {}", dce)
        } catch (e: Exception) {
            LOGGER.error("error on finding dce from BusinessMapping")
            e.printStackTrace()
        }

        //  SOAP request sending
        return MSUtils.sendSoap(UPDATE_CONSULTATION_URL, soapMessage)
    }


    //TODO:Intégrer la fonction dans ReceiverMS + sender dans DataCoreService delete ()
    fun deleteConsultation(login: String, password: String, pa: String, consultation: Consultation, url: String, businessMappingRepository: BusinessMappingRepository):String{

        val reference = consultation.reference.toString()

        val dce = (businessMappingRepository!!.findFirstByDcIdAndApplicationName(reference, "MS")).block()!!.businessId

        val soapMessage = MSUtils.generateDeleteConsultationLogRequest(login, password, pa, dce)

        return MSUtils.sendSoap(url, soapMessage)
    }


/*
**  Lot **
*/

    fun createLot(login:String, password:String, pa:String, lot: Lot, uri:String, url:String, businessMappingRepository:BusinessMappingRepository):String{

        // Lot data formatter
        val libelle = if(lot.libelle.length > 255) lot.libelle.substring(0,255) else lot.libelle
        val ordre = lot.ordre.toString()
        val numero = lot.numero.toString()

        //  Get consultation reference from uri
        val reference = uri.split("/")[8]

        //  get consultation dce (saved during consultation creation) from businessMappingRepository
        val dce = (businessMappingRepository!!.findFirstByDcIdAndApplicationName(reference, "MS")).block()!!.businessId
        LOGGER.debug("get dce {} ", dce)

        //  soap request and response
        val soapMessage = MSUtils.generateCreateLotLogRequest(login, password, pa, dce, libelle, ordre, numero)
        val response = MSUtils.sendSoap(url, soapMessage)

        //  cleLot parsed from response and saved in businessMapping
        val parseResponse = response.split( "&lt;propriete nom=\"cle_lot\"&gt;|&lt;/propriete&gt;".toRegex())
        val cleLot = parseResponse[2]

        val businessMappingLot = BusinessMapping(applicationName = "MSLot", businessId = cleLot, dcId = lot.uuid)
        businessMappingRepository.save(businessMappingLot).block()
        LOGGER.debug("saved businessMapping {} ", businessMappingLot)

        return response
    }

    //TODO: Intégrer dans ReceiverMS
    fun updateLot(login: String, password: String, pa: String, lot: Lot, uri: String, url: String, businessMappingRepository: BusinessMappingRepository):String{

        //  Lot data formatter
        val uuid = lot.uuid
        val libelle = if(lot.libelle.length > 255) lot.libelle.substring(0,255) else lot.libelle
        val ordre = lot.ordre.toString()
        val numero = lot.numero.toString()

        //  Get consultation reference from uri
        val reference = uri.split("/")[8]

        //get consultation dce (saved during consultation creation) from businessMappingRepository
        val dce = (businessMappingRepository!!.findFirstByDcIdAndApplicationName(reference, "MS")).block()!!.businessId

        //  get cleLot (saved during lot creation) from businessMappingRepository
        val cleLot = (businessMappingRepository!!.findFirstByDcIdAndApplicationName(uuid, "MSLot")).block()!!.businessId

        //soap request and response
        val soapMessage = MSUtils.generateModifyLotRequest(login, password, pa, dce, cleLot, libelle, ordre, numero)

        return MSUtils.sendSoap(url, soapMessage)
    }

    //TODO: Intégrer dans ReceiverMS
    fun deleteLot(login:String, password: String, pa: String, lot: Lot, uri: String, url: String, businessMappingRepository: BusinessMappingRepository):String{

        val uuid = lot.uuid

        //  Get consultation reference from uri
        val reference = uri.split("/")[8]

        //  Get consultation dce (saved during consultation creation) from businessMappingRepository
        val dce = (businessMappingRepository!!.findFirstByDcIdAndApplicationName(reference, "MS")).block()!!.businessId

        //  Get cleLot (saved during lot creation) from businessMappingRepository
        val cleLot =(businessMappingRepository!!.findFirstByDcIdAndApplicationName(uuid, "MSLot")).block()!!.businessId

        //SOAP request and response
        val soapMessage = MSUtils.generateDeleteLotRequest(login, password, pa, dce, cleLot)

        return MSUtils.sendSoap(url, soapMessage)
    }

    //TODO: Ou intégrer le service ?
    fun deleteAllLot(login: String, password: String, pa: String, uri: String, url: String, businessMappingRepository: BusinessMappingRepository):String{

        //  Get consultation reference from uri
        val reference = uri.split("/")[8]

        val dce = (businessMappingRepository!!.findFirstByDcIdAndApplicationName(reference, "MS")).block()!!.businessId

        //  SOAP request and response
        val soapMessage = MSUtils.generateDeleteAllLotRequest(login, password, pa, dce)

        return MSUtils.sendSoap(url, soapMessage)
    }

}