package org.ozwillo.dcimporter.service.marchesecurise

import org.ozwillo.dcimporter.model.BusinessMapping
import org.ozwillo.dcimporter.model.datacore.DCBusinessResourceLight
import org.ozwillo.dcimporter.model.marchepublic.Lot
import org.ozwillo.dcimporter.model.wsdl.marchesecurise.request.GenerateSoapRequest
import org.ozwillo.dcimporter.repository.BusinessMappingRepository
import org.ozwillo.dcimporter.service.marchesecurise.rabbitMQ.ReceiverMS
import org.ozwillo.dcimporter.web.marchesecurise.SendSoap
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

//TODO: Gestion des erreurs spécifiques aux requêtes
@Service
class CreateOrModifyLot(){

    private val login:String = ""
    private val password:String = ""
    private val pa:String = ""
    private val url:String = ""
    private val businessMappingRepository: BusinessMappingRepository? = null



    companion object {

        private val LOGGER: Logger = LoggerFactory.getLogger(ReceiverMS::class.java)

        fun createLot(login:String, password:String, pa:String, lot:Lot, uri:String, url:String, businessMappingRepository:BusinessMappingRepository):String{

            val libelle = if(lot.libelle.length > 255) lot.libelle.substring(0,255) else lot.libelle
            val ordre = lot.ordre.toString()
            val numero = lot.numero.toString()

            val reference = uri.split("/")[8]

            //find consultation dce (saved during consultation creation) from business mapping
            val dce = (businessMappingRepository!!.findFirstByDcIdAndApplicationName(reference, "MS")).block()!!.businessId
            LOGGER.debug("=== FOUND DCE : {} === ", dce)


            //soap request and response
            val soapMessage = GenerateSoapRequest.generateCreateLotLogRequest(login, password, pa, dce, libelle, ordre, numero)
            val response = SendSoap.sendSoap(url, soapMessage)

            //cleLot parsed from response and saved in businessMapping
            val parseResponse = response.split("&lt;propriete nom=\"cle_lot\"&gt;|&lt;/propriete&gt;".toRegex())
            val cleLot = parseResponse[2]

            val businessMappingLot = BusinessMapping(applicationName = "MSLot", businessId = cleLot, dcId = lot.uuid)
            businessMappingRepository.save(businessMappingLot).block()
            LOGGER.debug(" === SAVED BUSINESS MAPPING : {} ", businessMappingLot)

            return response
        }
    }



    //TODO: Intégrer dans MarchePublicHandler.updateLot() - Param = dcLot + reference(req.pathVariable("reference"))
    fun modifyLot(reference:String, dcLot: DCBusinessResourceLight):String{

        val lot:Lot = Lot.toLot(dcLot)

        val uuid = lot.uuid
        val libelle = if(lot.libelle.length > 255) lot.libelle.substring(0,255) else lot.libelle
        val ordre = lot.ordre.toString()
        val numero = lot.numero.toString()

        //find consultation dce (saved during consultation creation) from business mapping
        val dce = (businessMappingRepository!!.findByDcIdAndApplicationName(reference, "MS")).block()!!.businessId

        //find cleLot (saved during lot creation) from businessMapping
        val cleLot = (businessMappingRepository.findByDcIdAndApplicationName(uuid, "MSLot")).block()!!.businessId

        //soap request and response
        val soapMessage = GenerateSoapRequest.generateModifyLotRequest(login, password, pa, dce, cleLot, libelle, ordre, numero)

        return SendSoap.sendSoap(url, soapMessage)
    }

    //TODO: Intégrer dans MarchePublicHandler.deleteLot()
    fun deleteLot(reference: String, dcLot: DCBusinessResourceLight):String{
        val lot:Lot = Lot.toLot(dcLot)

        val uuid = lot.uuid

        //find consultation dce (saved during consultation creation) from business mapping
        val dce = (businessMappingRepository!!.findByDcIdAndApplicationName(reference, "MS")).block()!!.businessId
        //find cleLot (saved during lot creation) from businessMapping
        val cleLot =(businessMappingRepository.findByDcIdAndApplicationName(uuid, "MSLot")).block()!!.businessId

        //soap request and response
        val soapMessage = GenerateSoapRequest.generateDeleteLotRequest(login, password, pa, dce, cleLot)

        return SendSoap.sendSoap(url, soapMessage)
    }

    fun deleteAllLot(reference: String):String{
        val dce = (businessMappingRepository!!.findByDcIdAndApplicationName(reference, "MS")).block()!!.businessId

        val soapMessage = GenerateSoapRequest.generateDeleteAllLotRequest(login, password, pa, dce)
        return SendSoap.sendSoap(url, soapMessage)
    }
}