package org.ozwillo.dcimporter.model.wsdl.marchesecurise.request

import groovy.text.SimpleTemplateEngine
import org.ozwillo.dcimporter.util.DCUtils
import org.springframework.data.domain.AfterDomainEventPublication
import java.io.IOException
import java.security.Timestamp

class ModifyConsultationSoapRequest{

    companion object {
        fun generateModifyConsultationLogRequest(login:String, password:String, pa:String, dce:String, objet:String, enligne:String, datePublication:String, dateCloture:String, reference:String, finaliteMarche:String, typeMarche:String, prestation:String, passation:String, alloti:String, departement:String, email:String):String{
            val model = HashMap<String, String>()
            model["login"] = login
            model["password"] = password
            model["pa"] = pa
            model["dce"] = dce
            model["objet"] = objet
            model["enligne"] = enligne
            model["datePublication"] = datePublication
            model["dateCloture"] = dateCloture
            model["reference"] = reference
            model["finaliteMarche"] = finaliteMarche
            model["typeMarche"] = typeMarche
            model["prestation"] = prestation
            model["passation"] = passation
            model["alloti"] = alloti
            model["departement"] = departement
            model["email"] = email


            val engine = SimpleTemplateEngine()
            var result = ""
            try {
                result = engine.createTemplate(DCUtils.templateToString("templateModifyConsultationRequest.groovy")).make(model).toString()
            }catch (e:ClassNotFoundException){
                e.printStackTrace()
            }catch (e: IOException){
                e.printStackTrace()
            }
            return result
        }
    }
}