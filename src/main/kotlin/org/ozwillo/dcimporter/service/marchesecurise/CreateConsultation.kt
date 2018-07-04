package org.ozwillo.dcimporter.service.marchesecurise

import org.ozwillo.dcimporter.model.BusinessMapping
import org.ozwillo.dcimporter.model.datacore.DCBusinessResourceLight
import org.ozwillo.dcimporter.model.marchepublic.Consultation
import org.ozwillo.dcimporter.model.wsdl.marchesecurise.request.GenerateSoapRequest
import org.ozwillo.dcimporter.repository.BusinessMappingRepository
import org.ozwillo.dcimporter.util.DCUtils
import org.ozwillo.dcimporter.web.marchesecurise.SendSoap
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.sql.Timestamp

//TODO: Gestion des erreurs spécifiques au requêtes ? => mauvais format (http 500)
//TODO: Externalisation url ?
@Service
class CreateConsultation(){

    private val login:String = ""
    private val password:String = ""
    private val pa:String = ""
    private val businessMappingRepository: BusinessMappingRepository? = null

    private fun sendCreateConsultationRequest (url:String):String{
        val soapMessage = GenerateSoapRequest.generateCreateConsultationLogRequest(login, password,pa)

        return SendSoap.sendSoap(url, soapMessage)
    }

    private fun createConsultationAndSaveDce(url:String, dcConsultation: DCBusinessResourceLight):String{
        val response = sendCreateConsultationRequest(url)
        val parseResponse:List<String> = response.split("&lt;propriete nom=\"cle\" statut=\"changed\"&gt;|&lt;/propriete&gt;".toRegex())
        val dce = parseResponse[1]
        val businessMapping = BusinessMapping(applicationName = "MS", businessId = dce, dcId = dcConsultation.getStringValue("mpconsultation:reference"))
        businessMappingRepository!!.save(businessMapping).block()!!

        return response
    }

    //TODO:Intégrer la fonction dans MarchePublicHandler.create()
    fun createAndModifyConsultation(dcConsultation:DCBusinessResourceLight, url:String):String{

        val consultation:Consultation = Consultation.toConsultation(dcConsultation)

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

        createConsultationAndSaveDce(url, dcConsultation)

        val dce = (businessMappingRepository!!.findByDcIdAndApplicationName(reference, "MS")).block()!!.businessId

        val soapMessage = GenerateSoapRequest.generateModifyConsultationLogRequest(login, password, pa, dce, objet, enligne, datePublication, dateCloture, reference, finaliteMarche, typeMarche, prestation, passation, alloti, departement, email)

        return SendSoap.sendSoap(url, soapMessage)
    }

    //TODO:Intégrer la fonction dans MarchePublicHandler.update()
    fun modifyConsultation(dcConsultation: DCBusinessResourceLight, url: String):String{

        val consultation:Consultation = Consultation.toConsultation(dcConsultation)

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

        val dce = (businessMappingRepository!!.findByDcIdAndApplicationName(reference, "MS")).block()!!.businessId

        val soapMessage = GenerateSoapRequest.generateModifyConsultationLogRequest(login, password, pa, dce, objet, enligne, datePublication, dateCloture, reference, finaliteMarche, typeMarche, prestation, passation, alloti, departement, email)

        return SendSoap.sendSoap(url, soapMessage)
    }
}