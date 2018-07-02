package org.ozwillo.dcimporter.service.marchesecurise

import org.ozwillo.dcimporter.model.datacore.DCBusinessResourceLight
import org.ozwillo.dcimporter.model.marchepublic.Lot
import org.ozwillo.dcimporter.model.wsdl.marchesecurise.request.GenerateSoapRequest
import org.ozwillo.dcimporter.repository.BusinessMappingRepository
import org.ozwillo.dcimporter.web.marchesecurise.SendSoap
import org.springframework.stereotype.Service

//TODO: Gestion des erreurs spécifiques aux requêtes
@Service
class CreateOrModifyLot(private val login:String,
                        private val password:String,
                        private val pa:String,
                        private val url:String,
                        private val businessMappingRepository: BusinessMappingRepository){

    //TODO: Intégrer dans MarchePublicHandler.createLot - Param = dcLot + reference(req.pathVariable("reference"))
    fun createLot(reference:String, dcLot:DCBusinessResourceLight):String{

        val lot:Lot = Lot.toLot(dcLot)

        val libelle = if(lot.libelle.length > 255) lot.libelle.substring(0,255) else lot.libelle
        val ordre = lot.ordre.toString()
        val numero = lot.numero.toString()

        val businessMapping = businessMappingRepository.findByDcIdAndApplicationName(reference, "MS")
        val dce = businessMapping.block()!!.businessId

        val soapMessage = GenerateSoapRequest.generateCreateLotLogRequest(login, password, pa, dce, libelle, ordre, numero)

        return SendSoap.sendSoap(url, soapMessage)
    }

    //TODO: Intégrer dans MarchePublicHandler.updateLot() - Param = dcLot + reference(req.pathVariable("reference"))
    fun modifyLot(reference:String, dcLot: DCBusinessResourceLight):String{

        val lot:Lot = Lot.toLot(dcLot)

        val uuid = lot.uuid
        val libelle = if(lot.libelle.length > 255) lot.libelle.substring(0,255) else lot.libelle
        val ordre = lot.ordre.toString()
        val numero = lot.numero.toString()

        val businessMapping = businessMappingRepository.findByDcIdAndApplicationName(reference, "MS")
        val dce = businessMapping.block()!!.businessId

        val soapMessage = GenerateSoapRequest.generateModifyLotRequest(login, password, pa, dce, uuid, libelle, ordre, numero)

        return SendSoap.sendSoap(url, soapMessage)
    }

    //TODO: Intégrer dans MarchePublicHandler.deleteLot()
    fun deleteLot(reference: String, dcLot: DCBusinessResourceLight):String{
        val lot:Lot = Lot.toLot(dcLot)

        val uuid = lot.uuid

        val businessMapping = businessMappingRepository.findByDcIdAndApplicationName(reference, "MS")
        val dce = businessMapping.block()!!.businessId

        val soapMessage = GenerateSoapRequest.generateDeleteLotRequest(login, password, pa, dce, uuid)

        return SendSoap.sendSoap(url, soapMessage)
    }

    fun deleteAllLot(reference: String):String{
        val businessMapping = businessMappingRepository.findByDcIdAndApplicationName(reference, "MS")
        val dce = businessMapping.block()!!.businessId

        val soapMessage = GenerateSoapRequest.generateDeleteAllLotRequest(login, password, pa, dce)
        return SendSoap.sendSoap(url, soapMessage)
    }
}