package org.ozwillo.dcimporter.util

import groovy.text.SimpleTemplateEngine
import org.ozwillo.dcimporter.util.soap.response.parsing.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.core.io.ClassPathResource
import org.springframework.util.FileCopyUtils
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLConnection
import javax.xml.bind.JAXBContext
import javax.xml.bind.JAXBElement
import javax.xml.bind.Unmarshaller
import javax.xml.stream.XMLInputFactory
import javax.xml.stream.XMLStreamException
import javax.xml.stream.XMLStreamReader

class MSUtils{
    companion object {

        private val logger: Logger = LoggerFactory.getLogger(MSUtils::class.java)

        const val CONSULTATION_TYPE = "marchepublic:consultation_0"
        const val LOT_TYPE = "marchepublic:lot_0"
        const val PIECE_TYPE = "marchepublic:piece_0"

        fun convertOctetToMo(size:Int):Float{
            return size.toFloat()/1024/1024
        }

        private fun templateToString(templatePath:String):String {
            val resource = ClassPathResource(templatePath)
            val bos = ByteArrayOutputStream()

            try {
                FileCopyUtils.copy(resource.inputStream, bos)
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
            val response = resultMessage.toString()
            logger.debug("SOAP sending, response : {}", response.replace("&lt;", "<").replace("&gt;", ">"))
            return response
        }

        private fun initiliazeStreamReader(response: String): XMLStreamReader{
            val xif: XMLInputFactory = XMLInputFactory.newFactory()
            return xif.createXMLStreamReader(ByteArrayInputStream(response.toByteArray(Charsets.UTF_8)))
        }

        fun parseToResponseType (response: String, type: String, action: String, ref: String = ""): ResponseType{
            val returnResponse = parseToReturn(response, type, action)
            return if (!returnResponse.isEmpty()) parseReturnToObject(returnResponse, type, action, ref) else throw BadLogError("Unable to process $type $action request for following reason : Unknown login/password.")
        }

        private fun parseToReturn(response: String, type: String, action: String): String{
            try {
                val xsr = initiliazeStreamReader(response)
                xsr.nextTag()
                xsr.nextTag()
                xsr.nextTag()

                return when(type){
                    CONSULTATION_TYPE -> {
                        when(action){
                            BindingKeyAction.CREATE.value -> {
                                val jc: JAXBContext = JAXBContext.newInstance(CreateConsultationLogResponse::class.java)
                                val unmarshaller: Unmarshaller = jc.createUnmarshaller()
                                val createConsultationLogResponse: JAXBElement<CreateConsultationLogResponse> = unmarshaller.unmarshal(xsr, CreateConsultationLogResponse::class.java)

                                createConsultationLogResponse.value.soapReturn.toString()
                            }
                            BindingKeyAction.UPDATE.value -> {
                                val jc: JAXBContext = JAXBContext.newInstance(UpdateConsultationLogResponse::class.java)
                                val unmarshaller: Unmarshaller = jc.createUnmarshaller()
                                val createConsultationLogResponse: JAXBElement<UpdateConsultationLogResponse> = unmarshaller.unmarshal(xsr, UpdateConsultationLogResponse::class.java)

                                createConsultationLogResponse.value.soapReturn.toString()
                            }
                            BindingKeyAction.DELETE.value -> {
                                val jc: JAXBContext = JAXBContext.newInstance(DeleteConsultationResponse::class.java)
                                val unmarshaller: Unmarshaller = jc.createUnmarshaller()
                                val createConsultationLogResponse: JAXBElement<DeleteConsultationResponse> = unmarshaller.unmarshal(xsr, DeleteConsultationResponse::class.java)

                                createConsultationLogResponse.value.soapReturn.toString()
                            }
                            BindingKeyAction.CHECK.value -> {
                                val jc: JAXBContext = JAXBContext.newInstance(CheckConsultationResponse::class.java)
                                val unmarshaller: Unmarshaller = jc.createUnmarshaller()
                                val checkConsultationResponse: JAXBElement<CheckConsultationResponse> = unmarshaller.unmarshal(xsr, CheckConsultationResponse::class.java)

                                checkConsultationResponse.value.soapReturn.toString()
                            }
                            BindingKeyAction.PUBLISH.value -> {
                                val jc: JAXBContext = JAXBContext.newInstance(PublishConsultationResponse::class.java)
                                val unmarshaller: Unmarshaller = jc.createUnmarshaller()
                                val publishConsultationResponse: JAXBElement<PublishConsultationResponse> = unmarshaller.unmarshal(xsr, PublishConsultationResponse::class.java)

                                publishConsultationResponse.value.soapReturn.toString()
                            }
                            else -> {
                               logger.warn("Unable to recognize requested action")
                               ""
                            }
                        }
                    }
                    LOT_TYPE -> {
                        when(action){
                            BindingKeyAction.CREATE.value -> {
                                val jc: JAXBContext = JAXBContext.newInstance(CreateLotResponse::class.java)
                                val unmarshaller: Unmarshaller = jc.createUnmarshaller()
                                val createLotResponse: JAXBElement<CreateLotResponse> = unmarshaller.unmarshal(xsr, CreateLotResponse::class.java)

                                createLotResponse.value.soapReturn.toString()
                            }
                            BindingKeyAction.UPDATE.value -> {
                                val jc: JAXBContext = JAXBContext.newInstance(UpdateLotResponse::class.java)
                                val unmarshaller: Unmarshaller = jc.createUnmarshaller()
                                val updateLotResponse: JAXBElement<UpdateLotResponse> = unmarshaller.unmarshal(xsr, UpdateLotResponse::class.java)

                                updateLotResponse.value.soapReturn.toString()
                            }
                            BindingKeyAction.DELETE.value -> {
                                val jc: JAXBContext = JAXBContext.newInstance(DeleteLotResponse::class.java)
                                val unmarshaller: Unmarshaller = jc.createUnmarshaller()
                                val deleteLotResponse: JAXBElement<DeleteLotResponse> = unmarshaller.unmarshal(xsr, DeleteLotResponse::class.java)

                                deleteLotResponse.value.soapReturn.toString()
                            }
                            else -> {
                                logger.warn("Unable to recognize requested action")
                                ""
                            }
                        }
                    }
                    PIECE_TYPE -> {
                        when(action){
                            BindingKeyAction.CREATE.value -> {
                                val jc: JAXBContext = JAXBContext.newInstance(CreatePieceResponse::class.java)
                                val unmarshaller: Unmarshaller = jc.createUnmarshaller()
                                val createPieceResponse: JAXBElement<CreatePieceResponse> = unmarshaller.unmarshal(xsr, CreatePieceResponse::class.java)

                                return when(createPieceResponse.name.toString()){
                                    "{https://www.marches-securises.fr/webserv/}nouveau_fichier_logResponse" -> createPieceResponse.value.soapReturn.toString()
                                    "{http://schemas.xmlsoap.org/soap/envelope/}Fault" -> throw SoapParsingUnexpectedError("Unknown array type. Please check SOAP request.")
                                    else -> throw SoapParsingUnexpectedError("An error occurs during soap response parsing from Marchés Sécurisés. Please check your request format.")
                                }
                            }
                            BindingKeyAction.DELETE.value -> {
                                val jc: JAXBContext = JAXBContext.newInstance(DeletePieceResponse::class.java)
                                val unmarshaller: Unmarshaller = jc.createUnmarshaller()
                                val deletePieceResponse: JAXBElement<DeletePieceResponse> = unmarshaller.unmarshal(xsr, DeletePieceResponse::class.java)

                                deletePieceResponse.value.soapReturn.toString()
                            }
                            else -> {
                                logger.warn("Unable to recognize requested action")
                                ""
                            }
                        }
                    }
                    else -> {
                        logger.warn("Unable to recognize type")
                        ""
                    }
                }
            }catch (e: XMLStreamException){
                throw SoapParsingUnexpectedError("An error occurs during soap response parsing from Marchés Sécurisés. Please check your request format.")
            }
        }

        private fun parseReturnToObject(response: String, type: String, action: String, ref: String): ResponseType{
            return when (action) {
                BindingKeyAction.CREATE.value, BindingKeyAction.UPDATE.value -> {
                    when (type){
                        CONSULTATION_TYPE, LOT_TYPE -> {
                            val xsr = initiliazeStreamReader(response)
                            xsr.nextTag()

                            val jc: JAXBContext = JAXBContext.newInstance(Data::class.java)
                            val unmarshaller: Unmarshaller = jc.createUnmarshaller()
                            val dataJAXB: JAXBElement<Data> = unmarshaller.unmarshal(xsr, Data::class.java)

                            return when(dataJAXB.value.responseObject!!.size){
                                1 -> dataJAXB.value.responseObject!![0]
                                else -> {
                                    val index = dataJAXB.value.responseObject!!.indexOf(dataJAXB.value.responseObject!!.find { o -> o.properties!!.find { p -> (p.value.toString() == ref && (p.name == "ordre" || p.name == "nom"))} != null })
                                    if (index >= 0) dataJAXB.value.responseObject!![index] else throw SoapParsingUnexpectedError("An unexpected error occurs during SOAP response parsing preventing from processing request to Marchés Sécurisés. Please Check SOAP response.")
                                }
                            }
                        }
                        PIECE_TYPE -> {
                            val xsr = initiliazeStreamReader(response)
                            xsr.nextTag()

                            val jc: JAXBContext = JAXBContext.newInstance(Data::class.java)
                            val unmarshaller: Unmarshaller = jc.createUnmarshaller()
                            val dataJAXB: JAXBElement<Data> = unmarshaller.unmarshal(xsr, Data::class.java)

                            return when(dataJAXB.name.toString()){
                                "{interbat/framwork-exportation}data" -> {
                                    when(dataJAXB.value.responseObject!!.size){
                                        1 -> dataJAXB.value.responseObject!![0]
                                        else -> {
                                            val index = dataJAXB.value.responseObject!!.indexOf(dataJAXB.value.responseObject!!.find { o -> o.properties!!.find { p -> (p.value.toString() == ref && (p.name == "ordre" || p.name == "nom"))} != null })
                                            if (index >= 0) dataJAXB.value.responseObject!![index] else throw SoapParsingUnexpectedError("An unexpected error occurs during SOAP response parsing preventing from processing request to Marchés Sécurisés. Please Check SOAP response.")
                                        }
                                    }
                                }
                                "{interbat/erreur_tableau}creation_piece_error" -> throw SoapParsingUnexpectedError("Array error, please check SOAP request format. Array name must be \"fichier\" and must contains 8 key/value items (keys : lot, libelle, la, ordre, nom, extension, contenu and poids)")
                                "{interbat/erreur_identification}identification_file_error" -> throw BadLogError("Unable to process to piece creation in Marchés Sécurisés beacause of following error : unknown login/password")
                                "{interbat/erreur_crea_file}creation_file_error" -> {
                                    val xsrCreationPieceError = initiliazeStreamReader(response)
                                    xsrCreationPieceError.nextTag()
                                    xsrCreationPieceError.nextTag()

                                    val jcCreationPieceError: JAXBContext = JAXBContext.newInstance(Data::class.java)
                                    val unmarshallerCreationPieceError: Unmarshaller = jcCreationPieceError.createUnmarshaller()
                                    val dataJAXBCreationPieceError: JAXBElement<Data> = unmarshallerCreationPieceError.unmarshal(xsrCreationPieceError, Data::class.java)
                                    when(dataJAXBCreationPieceError.name.toString()){
                                        "{interbat/erreur_crea_file}consultation_non_trouvee" -> throw BadDceError("Unable to process to piece creation in Marchés Sécurisés beacause of following error : Bad Dce")
                                        "{interbat/erreur_crea_file}pa_non_trouvee" -> throw BadPaError("Unable to process to piece creation in Marchés Sécurisés beacause of following error : Bad Pa")
                                        else -> throw SoapParsingUnexpectedError("Unable to process to piece creation in Marchés Sécurisés beacause of unexpected error")
                                    }
                                }
                                else -> throw SoapParsingUnexpectedError("An unexpected error occurs during SOAP response parsing preventing from processing request to Marchés Sécurisés. Please Check SOAP response.")
                            }
                        }
                        else -> throw SoapParsingUnexpectedError("Unable to recognize type")
                    }
                }
                BindingKeyAction.DELETE.value -> {
                    when (type){
                        CONSULTATION_TYPE -> {
                            val xsr = initiliazeStreamReader(response)
                            xsr.nextTag()
                            xsr.nextTag()

                            val jc: JAXBContext = JAXBContext.newInstance(DeleteConsultationOkState::class.java)
                            val unmarshaller: Unmarshaller = jc.createUnmarshaller()
                            val dataJAXB: JAXBElement<DeleteConsultationOkState> = unmarshaller.unmarshal(xsr, DeleteConsultationOkState::class.java)
                            val deleteConsultationOkState: DeleteConsultationOkState = dataJAXB.value

                            when(dataJAXB.name.toString()){
                                "{interbat/suppression_effectuee}consultation_suppr_ok" ->  deleteConsultationOkState
                                "{interbat/suppression_refusee}consultation_cle_error" -> throw BadDceError("Unable to process to consultation deletion in Marchés Sécurisés beacause of following error : Bad Dce")
                                "{interbat/pa_refuse}pa_suppr_dce_error" -> throw BadPaError("Unable to process to consultation deletion in Marchés Sécurisés beacause of following error : Bad Pa")
                                "{interbat/log_refuse}log_error" -> throw BadLogError("Unable to process to consultation deletion in Marchés Sécurisés beacause of following error : unknown login/password")
                                else -> throw SoapParsingUnexpectedError("Unable to process to consultation deletion in Marchés Sécurisés because of unexpected error")
                            }
                        }
                        LOT_TYPE, PIECE_TYPE -> {
                            val xsr = initiliazeStreamReader(response)
                            xsr.nextTag()

                            val jc: JAXBContext = JAXBContext.newInstance(Data::class.java)
                            val unmarshaller: Unmarshaller = jc.createUnmarshaller()
                            val dataJAXB: JAXBElement<Data> = unmarshaller.unmarshal(xsr, Data::class.java)

                            when(dataJAXB.name.toString()){
                                "{interbat/framwork-exportation}data" -> {
                                    val checkDeletion = dataJAXB.value.responseObject!!.find { o -> o.properties!!.find { p -> p.value.toString() == ref } != null }
                                    if (checkDeletion == null) dataJAXB.value.responseObject!![0] else throw DeletionError("An unexpected error occurs preventing from delete object from Marchés Sécurisés. Please check SOAP response.")
                                }
                                "{interbat/erreur_cle_piece}cle_piece_error" -> throw BadClePiece("Unable to process to piece deletion in Marchés Sécurisés beacause of following error : requested piece is not found")
                                "{interbat/erreur_cle_dce}cle_dce_error" -> throw BadDceError("Unable to process to piece deletion in Marchés Sécurisés beacause of following error : Bad Dce")
                                "{interbat/erreur_crea_lot}creation_lot_error" -> throw BadPaError("Unable to process to piece deletion in Marchés Sécurisés beacause of following error : Bad Pa")
                                "{interbat/erreur_identification}identification_lot_error" -> throw BadLogError("Unable to process to piece deletion in Marchés Sécurisés beacause of following error : unknown login/password")
                                else -> throw SoapParsingUnexpectedError("Unable to process to object deletion in Marchés Sécurisés because of unexpected error")
                            }
                        }
                        else -> throw SoapParsingUnexpectedError("Unable to recognize type")
                    }
                }
                BindingKeyAction.CHECK.value -> {
                    val xsr = initiliazeStreamReader(response)
                    xsr.nextTag()

                    val jc: JAXBContext = JAXBContext.newInstance(Data::class.java)
                    val unmarshaller: Unmarshaller = jc.createUnmarshaller()
                    val dataJAXB: JAXBElement<Data> = unmarshaller.unmarshal(xsr, Data::class.java)

                    when(dataJAXB.name.toString()){
                        "{interbat/framwork-exportation}data" -> dataJAXB.value.responseObject!![0]
                        "{interbat/validation_refusee}validation_error" -> {
                            val xsrCheckRejected = initiliazeStreamReader(response)
                            xsrCheckRejected.nextTag()
                            xsrCheckRejected.nextTag()

                            val jcCheckRejected: JAXBContext = JAXBContext.newInstance(CheckConsultationRejectedState::class.java)
                            val unmarshallerCheckRejected: Unmarshaller = jcCheckRejected.createUnmarshaller()
                            val dataJAXBCheckRejected: JAXBElement<CheckConsultationRejectedState> = unmarshallerCheckRejected.unmarshal(xsrCheckRejected, CheckConsultationRejectedState::class.java)
                            throw CheckConsultationRejectedError("Consultation publication request was rejected because of the following error : ${dataJAXBCheckRejected.value.errorState}\n" +
                                    "Consultation saved data : \n" +
                                    "dce : ${dataJAXBCheckRejected.value.dce}\n" +
                                    "object : ${dataJAXBCheckRejected.value.objet}\n" +
                                    "date of publication : ${dataJAXBCheckRejected.value.datePublicationF}\n" +
                                    "date of closing : ${dataJAXBCheckRejected.value.dateClotureF}\n" +
                                    "reference : ${dataJAXBCheckRejected.value.reference}\n" +
                                    "market finality : ${dataJAXBCheckRejected.value.finaliteMarche}\n" +
                                    "market type : ${dataJAXBCheckRejected.value.typeMarche}\n" +
                                    "performance : ${dataJAXBCheckRejected.value.prestation}\n" +
                                    "list of department : ${dataJAXBCheckRejected.value.departement}\n" +
                                    "info : ${dataJAXBCheckRejected.value.informatique}\n" +
                                    "emails list : ${dataJAXBCheckRejected.value.emails}\n" +
                                    "online : ${dataJAXBCheckRejected.value.enLigne}\n" +
                                    "with lots : ${dataJAXBCheckRejected.value.alloti}\n" +
                                    "invisible : ${dataJAXBCheckRejected.value.invisible}\n")
                        }
                        "{interbat/dce_refusee}dce_error" -> throw BadDceError("Unable to process to consultation publication in Marchés Sécurisés beacause of following error : Bad Dce")
                        "{interbat/pa_refusee}pa_error" -> throw BadPaError("Unable to process to consultation publication in Marchés Sécurisés beacause of following error : Bad Pa")
                        "{interbat/identification_refusee}identification_error" -> throw BadLogError("Unable to process to consultation publication in Marchés Sécurisés beacause of following error : unknown login/password")
                        else -> throw SoapParsingUnexpectedError("Unable to check consultation for publication in Marchés Sécurisés beacause of unexpected error")
                    }
                }
                BindingKeyAction.PUBLISH.value -> {
                    val xsr = initiliazeStreamReader(response)
                    xsr.nextTag()

                    val jc: JAXBContext = JAXBContext.newInstance(Data::class.java)
                    val unmarshaller: Unmarshaller = jc.createUnmarshaller()
                    val dataJAXB: JAXBElement<Data> = unmarshaller.unmarshal(xsr, Data::class.java)

                    when(dataJAXB.name.toString()){
                        "{interbat/framwork-exportation}data" -> dataJAXB.value.responseObject!![0]
                        "{interbat/publication_refusee}publication_error" -> {
                            val xsrPublishRejected = initiliazeStreamReader(response)
                            xsrPublishRejected.nextTag()
                            xsrPublishRejected.nextTag()

                            val jcPublishRejected: JAXBContext = JAXBContext.newInstance(PublishConsultationRejectedState::class.java)
                            val unmarshallerPublishRejected: Unmarshaller = jcPublishRejected.createUnmarshaller()
                            val dataJAXBPublishRejected: JAXBElement<PublishConsultationRejectedState> = unmarshallerPublishRejected.unmarshal(xsrPublishRejected, PublishConsultationRejectedState::class.java)
                            throw PublishConsultationRejectedError("Consultation publication request was rejected \n" +
                                    "Consultation saved data : \n" +
                                    "dce : ${dataJAXBPublishRejected.value.dce}\n" +
                                    "object : ${dataJAXBPublishRejected.value.objet}\n" +
                                    "date of publication : ${dataJAXBPublishRejected.value.datePublicationF}\n" +
                                    "date of closing : ${dataJAXBPublishRejected.value.dateClotureF}\n" +
                                    "reference : ${dataJAXBPublishRejected.value.reference}\n" +
                                    "market finality : ${dataJAXBPublishRejected.value.finaliteMarche}\n" +
                                    "market type : ${dataJAXBPublishRejected.value.typeMarche}\n" +
                                    "performance : ${dataJAXBPublishRejected.value.prestation}\n" +
                                    "list of department : ${dataJAXBPublishRejected.value.departement}\n" +
                                    "info : ${dataJAXBPublishRejected.value.informatique}\n" +
                                    "emails list : ${dataJAXBPublishRejected.value.emails}\n" +
                                    "online : ${dataJAXBPublishRejected.value.enLigne}\n" +
                                    "with lots : ${dataJAXBPublishRejected.value.alloti}\n" +
                                    "invisible : ${dataJAXBPublishRejected.value.invisible}\n")
                        }
                        else -> throw SoapParsingUnexpectedError("Unable to process to consultation publication in Marchés Sécurisés beacause of unexpected error")
                    }
                }
                else -> throw SoapParsingUnexpectedError("Unable to recognize requested action")
            }
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

class BadDceError (override val message: String): Exception(message)
class BadPaError (override val message: String): Exception(message)
class BadLogError (override val message: String): Exception(message)
class CheckConsultationRejectedError (override val message: String): Exception(message)
class PublishConsultationRejectedError (override val message: String): Exception(message)
class ConsultationDuplicateError (override val message: String): Exception(message)
class LotDuplicateError (override val message: String): Exception(message)
class DeletionError (override val message: String): Exception(message)
class BadClePiece (override val message: String): Exception(message)
class PieceSizeError (override val message: String): Exception(message)
class SoapParsingUnexpectedError (override val message: String): Exception(message)