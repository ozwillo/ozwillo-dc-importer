package org.ozwillo.dcimporter.util

import groovy.text.SimpleTemplateEngine
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.core.io.ClassPathResource
import org.springframework.util.FileCopyUtils
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLConnection

class MSUtils{
    companion object {

        private val logger: Logger = LoggerFactory.getLogger(MSUtils::class.java)

        fun convertOctetToMo(size:Int):Float{
            return size.toFloat()/1024/1024
        }

        fun templateToString(templatePath:String):String {
            val resource = ClassPathResource(templatePath)
            val bos = ByteArrayOutputStream()

            try {
                FileCopyUtils.copy(resource.getInputStream(), bos)
            }catch (e:IOException){
                e.printStackTrace()
            }

            return bos.toString()
        }

        fun booleanToInt(bool:Boolean):Int{
            return if(bool)
                1
            else
                0
        }

        fun intListToString(ints:List<Int>):String{
            var result = ""
            for (index in ints.indices){
                result+="${ints[index]};"
            }
            result=result.substring(0,result.length-1)
            return result
        }

        fun stringListToString(stringList:List<String>):String{
            var result = ""
            for (index in stringList.indices){
                result+="${stringList[index]};"
            }
            result=result.substring(0,result.length-1)
            return result
        }

        @Throws(Exception::class)
        fun sendSoap(soapUrl:String, soapMessage:String):String{
            val url = URL(soapUrl)
            val connection: URLConnection = url.openConnection()
            val httpConnection: HttpURLConnection = connection as HttpURLConnection

            val byteArray:ByteArray = soapMessage.toByteArray()

            httpConnection.setRequestProperty("Content-Length", byteArray.size.toString())
            httpConnection.setRequestProperty("Content-Type", "text/xml; charset=utf8")
            httpConnection.setRequestProperty("SOAPAction", "")
            httpConnection.requestMethod = "POST"

            httpConnection.doOutput = true
            httpConnection.doInput = true

            val out: OutputStream = httpConnection.outputStream
            out.write(byteArray)
            out.close()
            var input: BufferedReader? = null
            val resultMessage = StringBuffer()
            try {
                val isr = InputStreamReader(httpConnection.inputStream)
                input = BufferedReader(isr)
                input.use {
                    var inputLine = it.readLine()
                    while (inputLine != null){
                        resultMessage.append(inputLine)
                        inputLine = it.readLine()
                    }
                    it.close()
                }
            }catch (e:Exception){
                logger.error(e.message)
            }finally {
                input?.close()
            }
            val response = resultMessage.toString().replace("&lt;", "<").replace("&gt;", ">")
            logger.debug("SOAP sending, response : {}", response)
            return response
        }

        fun generateCreateConsultationLogRequest(login:String, password:String, pa:String):String{
            val model = HashMap<String, String>()
            model["login"] = login
            model["password"] = password
            model["pa"] = pa

            val engine = SimpleTemplateEngine()
            var result = ""
            try {
                result = engine.createTemplate(templateToString("template/templateCreateConsultationLogRequest.groovy")).make(model).toString()
            }catch (e:ClassNotFoundException){
                e.printStackTrace()
            }catch (e: IOException){
                e.printStackTrace()
            }
            return result
        }

        fun generateModifyConsultationLogRequest(login:String, password:String, pa:String, dce:String, objet:String, enligne:String, datePublication:String, dateCloture:String, reference:String, finaliteMarche:String, typeMarche:String, prestation:String, passation:String, informatique:String, alloti:String, departement:String, email:String):String{
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
            model["informatique"] = informatique
            model["alloti"] = alloti
            model["departement"] = departement
            model["email"] = email


            val engine = SimpleTemplateEngine()
            var result = ""
            try {
                result = engine.createTemplate(templateToString("template/templateModifyConsultationRequest.groovy")).make(model).toString()
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
                result = engine.createTemplate(templateToString("template/templateDeleteConsultation.groovy")).make(model).toString()
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
                result = engine.createTemplate(templateToString("template/templateCreateLotRequest.groovy")).make(model).toString()
            }catch (e:ClassNotFoundException){
                e.printStackTrace()
            }catch (e: IOException){
                e.printStackTrace()
            }
            return result
        }

        fun generateModifyLotRequest(login: String, password: String, pa: String, dce: String, cleLot:String, libelle: String, ordre: String, numero: String):String{
            val model = HashMap<String, String>()
            model["login"] = login
            model["password"] = password
            model["pa"] = pa
            model["dce"] = dce
            model["cleLot"] = cleLot
            model["libelle"] = libelle
            model["ordre"] = ordre
            model["numero"] = numero

            val engine = SimpleTemplateEngine()
            var result = ""
            try {
                result = engine.createTemplate(templateToString("template/templateModifyLotRequest.groovy")).make(model).toString()
            }catch (e:ClassNotFoundException){
                e.printStackTrace()
            }catch (e: IOException){
                e.printStackTrace()
            }
            return result
        }

        fun generateDeleteLotRequest(login: String, password: String, pa: String, dce: String, cleLot: String):String{
            val model = HashMap<String, String>()
            model["login"] = login
            model["password"] = password
            model["pa"] = pa
            model["dce"] = dce
            model["cleLot"] = cleLot

            val engine = SimpleTemplateEngine()
            var result = ""
            try {
                result = engine.createTemplate(templateToString("template/templateDeleteLotRequest.groovy")).make(model).toString()
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
                result = engine.createTemplate(templateToString("template/templateDeleteAllLotRequest.groovy")).make(model).toString()
            }catch (e:ClassNotFoundException){
                e.printStackTrace()
            }catch (e: IOException){
                e.printStackTrace()
            }
            return result
        }

        fun generateCreatePieceLogRequest(login: String, password: String, pa: String, dce: String, cleLot:String, libelle:String, la:String, ordre:String, nom:String, extension:String, contenu:String, poids:String):String{
            val model = HashMap<String, String>()
            model["login"] = login
            model["password"] = password
            model["pa"] = pa
            model["dce"] = dce
            model["cleLot"] = cleLot
            model["libelle"] =libelle
            model["la"] = la
            model["ordre"] = ordre
            model["nom"] = nom
            model["extension"] = extension
            model["contenu"] = contenu
            model["poids"] = poids

            val engine = SimpleTemplateEngine()
            var result = ""
            try {
                result = engine.createTemplate(templateToString("template/templateCreatePieceRequest.groovy")).make(model).toString()
            }catch (e:ClassNotFoundException){
                e.printStackTrace()
            }catch (e: IOException){
                e.printStackTrace()
            }
            return result
        }

        fun generateModifyPieceLogRequest(login: String, password: String, pa: String, dce: String, clePiece:String, cleLot: String, libelle :String, ordre:String, nom:String, extension:String, contenu:String, poids:String):String{
            val model = HashMap<String, String>()
            model["login"] = login
            model["password"] = password
            model["pa"] = pa
            model["dce"] = dce
            model["clePiece"] = clePiece
            model["cleLot"] = cleLot
            model["libelle"] =libelle
            model["ordre"] = ordre
            model["nom"] = nom
            model["extension"] = extension
            model["contenu"] = contenu
            model["poids"] = poids

            val engine = SimpleTemplateEngine()
            var result = ""
            try {
                result = engine.createTemplate(templateToString("template/templateModifyPieceRequest.groovy")).make(model).toString()
            }catch (e:ClassNotFoundException){
                e.printStackTrace()
            }catch (e: IOException){
                e.printStackTrace()
            }
            return result
        }

        fun generateDeletePieceRequest(login: String, password: String, pa: String, dce: String, clePiece:String):String{
            val model = HashMap<String, String>()
            model["login"] = login
            model["password"] = password
            model["pa"] = pa
            model["dce"] = dce
            model["clePiece"] = clePiece

            val engine = SimpleTemplateEngine()
            var result = ""
            try {
                result = engine.createTemplate(templateToString("template/templateDeletePieceRequest.groovy")).make(model).toString()
            }catch (e:ClassNotFoundException){
                e.printStackTrace()
            }catch (e: IOException){
                e.printStackTrace()
            }
            return result
        }

        fun generateCheckConsultationRequest(login: String, password: String, pa: String, dce: String):String{
            val model = HashMap<String, String>()
            model["login"] = login
            model["password"] = password
            model["pa"] = pa
            model["dce"] = dce

            val engine = SimpleTemplateEngine()
            var result = ""
            try {
                result = engine.createTemplate(templateToString("template/generateCheckConsultationRequest.groovy")).make(model).toString()
            }catch (e:ClassNotFoundException){
                e.printStackTrace()
            }catch (e: IOException){
                e.printStackTrace()
            }
            return result
        }

        fun generatePublishConsultationRequest(login: String, password: String, pa: String, dce: String):String{
            val model = HashMap<String, String>()
            model["login"] = login
            model["password"] = password
            model["pa"] = pa
            model["dce"] = dce

            val engine = SimpleTemplateEngine()
            var result = ""
            try {
                result = engine.createTemplate(templateToString("template/templatePublishConsultation.groovy")).make(model).toString()
            }catch (e:ClassNotFoundException){
                e.printStackTrace()
            }catch (e: IOException){
                e.printStackTrace()
            }
            return result
        }
    }
}