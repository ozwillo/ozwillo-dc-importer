package org.ozwillo.dcimporter.service.marchesecurise

import org.ozwillo.dcimporter.model.datacore.DCBusinessResourceLight
import org.ozwillo.dcimporter.model.marchepublic.Consultation
import org.ozwillo.dcimporter.model.wsdl.marchesecurise.request.GenerateSoapRequest
import org.ozwillo.dcimporter.repository.BusinessMappingRepository
import org.ozwillo.dcimporter.web.marchesecurise.SendSoap
import org.springframework.stereotype.Service

//TODO: Gestion des erreurs spécifiques aux requêtes ?
@Service
class DeleteConsultation(private val login:String,
                         private val password:String,
                         private val pa:String,
                         private val url:String,
                         private val businessMappingRepository: BusinessMappingRepository){

    //TODO: Intégrer dans MarchePublicHandler.
    fun deleteConsultation(dcConsultation: DCBusinessResourceLight):String{

        val consultation:Consultation = Consultation.toConsultation(dcConsultation)

        val reference = consultation.reference.toString()

        val businessMapping = businessMappingRepository.findByDcIdAndApplicationName(reference, "MS")
        val dce = businessMapping.block()!!.businessId

        val soapMessage = GenerateSoapRequest.generateDeleteConsultationLogRequest(login, password, pa, dce)

        return SendSoap.sendSoap(url, soapMessage)
    }

}