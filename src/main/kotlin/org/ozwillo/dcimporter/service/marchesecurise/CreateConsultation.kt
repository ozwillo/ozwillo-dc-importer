package org.ozwillo.dcimporter.service.marchesecurise

import org.ozwillo.dcimporter.model.datacore.DCBusinessResourceLight
import org.ozwillo.dcimporter.model.marchepublic.Consultation
import org.ozwillo.dcimporter.model.wsdl.marchesecurise.request.CreateConsultationSoapRequest
import org.ozwillo.dcimporter.model.wsdl.marchesecurise.request.ModifyConsultationSoapRequest
import org.ozwillo.dcimporter.util.DCUtils
import org.ozwillo.dcimporter.web.marchesecurise.SendSoap
import org.springframework.stereotype.Service
import java.sql.Timestamp

@Service
class CreateConsultation(private val login:String, private val password:String, private val pa:String){

    private fun sendCreateConsultationRequest (url:String):String{
        val soapMessage = CreateConsultationSoapRequest.generateCreateConsultationLogRequest(login, password,pa)

        return SendSoap.sendSoap(url, soapMessage)
    }

    private fun getDce(url:String):String{
        val parseResponse:List<String> = sendCreateConsultationRequest(url).split("&lt;propriete nom=\"cle\" statut=\"changed\"&gt;|&lt;/propriete&gt;".toRegex())
        return parseResponse[1]
    }

    fun modifyConsultation(dcConsultation:DCBusinessResourceLight, url:String):String{

        val consultation:Consultation = Consultation.toConsultation(dcConsultation)

        val objet = if((consultation.objet).length > 255) (consultation.objet).substring(0,255) else consultation.objet
        val enligne = DCUtils.booleanToInt(consultation.enLigne).toString()
        val datePublication = ((Timestamp.valueOf(consultation.datePublication).time)/1000).toString()
        val dateCloture = ((Timestamp.valueOf(consultation.dateCloture).time)/1000).toString()
        val reference = if ((consultation.reference.toString()).length > 255) (consultation.reference.toString()).substring(0,255) else consultation.reference.toString()
        val finaliteMarche = (consultation.finaliteMarche).toString().toLowerCase()
        val typeMarche = (consultation.typeMarche).toString().toLowerCase()
        val prestation = (consultation.typePrestation).toString().toLowerCase()
        val passation = consultation.passation
        val alloti = DCUtils.booleanToInt(consultation.alloti).toString()
        val departement = DCUtils.intListToString(consultation.departementsPrestation)
        val email = if((DCUtils.stringListToString(consultation.emails)).length > 255) (DCUtils.stringListToString(consultation.emails)).substring(0,255) else DCUtils.stringListToString(consultation.emails)

        val dce = getDce(url)
        val soapMessage = ModifyConsultationSoapRequest.generateModifyConsultationLogRequest(login, password, pa, dce, objet, enligne, datePublication, dateCloture, reference, finaliteMarche, typeMarche, prestation, passation, alloti, departement, email)

        return SendSoap.sendSoap(url, soapMessage)
    }
}