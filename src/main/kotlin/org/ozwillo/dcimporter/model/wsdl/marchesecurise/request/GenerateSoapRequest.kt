package org.ozwillo.dcimporter.model.wsdl.marchesecurise.request

import groovy.text.SimpleTemplateEngine
import org.ozwillo.dcimporter.util.DCUtils
import java.io.IOException

class GenerateSoapRequest{

    companion object {
        fun generateCreateConsultationLogRequest(login:String, password:String, pa:String):String{
            val model = HashMap<String, String>()
            model["login"] = login
            model["password"] = password
            model["pa"] = pa

            val engine = SimpleTemplateEngine()
            var result = ""
            try {
                result = engine.createTemplate(DCUtils.templateToString("template/templateCreateConsultationLogRequest.groovy")).make(model).toString()
            }catch (e:ClassNotFoundException){
                e.printStackTrace()
            }catch (e: IOException){
                e.printStackTrace()
            }
            return result
        }

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
                result = engine.createTemplate(DCUtils.templateToString("template/templateModifyConsultationRequest.groovy")).make(model).toString()
            }catch (e:ClassNotFoundException){
                e.printStackTrace()
            }catch (e: IOException){
                e.printStackTrace()
            }
            return result
        }

        fun generateDeleteConsultationLogRequest(login: String, password: String, pa: String, dce: String):String{
            val model = HashMap<String, String>()
            model["login"] = login
            model["password"] = password
            model["pa"] = pa
            model["dce"] = dce

            val engine = SimpleTemplateEngine()
            var result = ""
            try {
                result = engine.createTemplate(DCUtils.templateToString("template/templateDeleteConsultation.groovy")).make(model).toString()
            }catch (e:ClassNotFoundException){
                e.printStackTrace()
            }catch (e: IOException){
                e.printStackTrace()
            }
            return result
        }

        fun generateCreateLotLogRequest(login: String, password: String, pa: String, dce: String, libelle:String, ordre:String, numero:String):String{
            val model = HashMap<String, String>()
            model["login"] = login
            model["password"] = password
            model["pa"] = pa
            model["dce"] = dce
            model["libelle"] =libelle
            model["ordre"] = ordre
            model["numero"] = numero

            val engine = SimpleTemplateEngine()
            var result = ""
            try {
                result = engine.createTemplate(DCUtils.templateToString("template/templateCreateLotRequest.groovy")).make(model).toString()
            }catch (e:ClassNotFoundException){
                e.printStackTrace()
            }catch (e: IOException){
                e.printStackTrace()
            }
            return result
        }

        fun generateModifyLotRequest(login: String, password: String, pa: String, dce: String, uuid:String, libelle: String, ordre: String, numero: String):String{
            val model = HashMap<String, String>()
            model["login"] = login
            model["password"] = password
            model["pa"] = pa
            model["dce"] = dce
            model["uuid"] = uuid
            model["libelle"] =libelle
            model["ordre"] = ordre
            model["numero"] = numero

            val engine = SimpleTemplateEngine()
            var result = ""
            try {
                result = engine.createTemplate(DCUtils.templateToString("template/templateModifyLotRequest.groovy")).make(model).toString()
            }catch (e:ClassNotFoundException){
                e.printStackTrace()
            }catch (e: IOException){
                e.printStackTrace()
            }
            return result
        }

        fun generateDeleteLotRequest(login: String, password: String, pa: String, dce: String, uuid: String):String{
            val model = HashMap<String, String>()
            model["login"] = login
            model["password"] = password
            model["pa"] = pa
            model["dce"] = dce
            model["uuid"] = uuid

            val engine = SimpleTemplateEngine()
            var result = ""
            try {
                result = engine.createTemplate(DCUtils.templateToString("template/templateDeleteLotRequest.groovy")).make(model).toString()
            }catch (e:ClassNotFoundException){
                e.printStackTrace()
            }catch (e: IOException){
                e.printStackTrace()
            }
            return result
        }

        fun generateDeleteAllLotRequest(login: String, password: String, pa: String, dce: String):String{
            val model = HashMap<String, String>()
            model["login"] = login
            model["password"] = password
            model["pa"] = pa
            model["dce"] = dce

            val engine = SimpleTemplateEngine()
            var result = ""
            try {
                result = engine.createTemplate(DCUtils.templateToString("template/templateDeleteAllLotRequest.groovy")).make(model).toString()
            }catch (e:ClassNotFoundException){
                e.printStackTrace()
            }catch (e: IOException){
                e.printStackTrace()
            }
            return result
        }
    }
}