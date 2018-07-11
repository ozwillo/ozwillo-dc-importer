package org.ozwillo.dcimporter.service.marchesecurise

import org.ozwillo.dcimporter.model.BusinessMapping
import org.ozwillo.dcimporter.model.datacore.DCBusinessResourceLight
import org.ozwillo.dcimporter.model.marchepublic.Consultation
import org.ozwillo.dcimporter.model.wsdl.marchesecurise.request.GenerateSoapRequest
import org.ozwillo.dcimporter.repository.BusinessMappingRepository
import org.ozwillo.dcimporter.service.marchesecurise.rabbitMQ.ReceiverMS
import org.ozwillo.dcimporter.util.DCUtils
import org.ozwillo.dcimporter.web.marchesecurise.MarcheSecuriseURL
import org.ozwillo.dcimporter.web.marchesecurise.SendSoap
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.lang.reflect.InvocationTargetException
import java.sql.Timestamp

//TODO: Gestion des erreurs spécifiques au requêtes ? => mauvais format (http 500)
@Service
class CreateConsultation(){

    companion object {

        private val LOGGER: Logger = LoggerFactory.getLogger(ReceiverMS::class.java)

        private fun sendCreateConsultationRequest (login:String, password:String, pa:String, url:String):String{
            val soapMessage = GenerateSoapRequest.generateCreateConsultationLogRequest(login, password,pa)
            return SendSoap.sendSoap(url, soapMessage)
        }

        private fun createConsultationAndSaveDce(login:String, password:String, pa:String, url:String, uri:String, businessMappingRepository: BusinessMappingRepository):String{
            val response = sendCreateConsultationRequest(login, password, pa, url)
            val parseResponse:List<String> = response.split("&lt;propriete nom=\"cle\" statut=\"changed\"&gt;|&lt;/propriete&gt;".toRegex())
            val dce = parseResponse[1]
            val businessMapping = BusinessMapping(applicationName = "MS", businessId = dce, dcId = uri)
            businessMappingRepository!!.save(businessMapping)


            return response
        }


        fun createAndModifyConsultation(login:String, password:String, pa:String, consultation: Consultation, uri:String, url:String, businessMappingRepository: BusinessMappingRepository):String{

            //val consultation:Consultation = Consultation.toConsultation(dcConsultation)

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

            val response = createConsultationAndSaveDce(login, password, pa, url, uri, businessMappingRepository)
            var soapMessage = ""
            try {
                val savedMonoBusinessMapping = businessMappingRepository.findFirstByDcIdAndApplicationName(uri, "MS")
                var dce: String = savedMonoBusinessMapping.block()!!.businessId
                soapMessage = GenerateSoapRequest.generateModifyConsultationLogRequest(login, password, pa, dce, objet, enligne, datePublication, dateCloture, reference, finaliteMarche, typeMarche, prestation, passation, alloti, departement, email)


                LOGGER.debug("==== SAVED BUSINESSMAPPING : {} with dce {}", savedMonoBusinessMapping, dce)
            } catch (e: Exception) {
                LOGGER.error("error mono business")
            }
            return SendSoap.sendSoap(MarcheSecuriseURL.MODIFY_CONSULTATION_URL, soapMessage)
        }
    }

    //TODO:Intégrer la fonction dans MarchePublicHandler.update()
    fun modifyConsultation(login:String, password:String, pa:String, consultation:Consultation, url: String, businessMappingRepository: BusinessMappingRepository):String{

        //val consultation:Consultation = Consultation.toConsultation(dcConsultation)

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

        val soapMessage = GenerateSoapRequest.generateModifyConsultationLogRequest(login, password, pa, dce, objet, enligne, datePublication, dateCloture, reference, finaliteMarche, typeMarche, prestation, passation, alloti, departement, email)

        return SendSoap.sendSoap(url, soapMessage)
    }
}