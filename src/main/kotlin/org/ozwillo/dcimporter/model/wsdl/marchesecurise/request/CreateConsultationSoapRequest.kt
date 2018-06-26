package org.ozwillo.dcimporter.model.wsdl.marchesecurise.request

import groovy.text.SimpleTemplateEngine
import org.ozwillo.dcimporter.util.DCUtils
import java.io.IOException

class CreateConsultationSoapRequest{

    companion object {
        fun generateCreateConsultationLogRequest(login:String, password:String, pa:String):String{
            val model = HashMap<String, String>()
            model.put("login", login)
            model.put("password", password)
            model.put("pa", pa)

            val engine = SimpleTemplateEngine()
            var result = ""
            try {
                result = engine.createTemplate(DCUtils.templateToString("templateCreateConsultationLogRequest.groovy")).make(model).toString()
            }catch (e:ClassNotFoundException){
                e.printStackTrace()
            }catch (e: IOException){
                e.printStackTrace()
            }
            return result
        }
    }
}