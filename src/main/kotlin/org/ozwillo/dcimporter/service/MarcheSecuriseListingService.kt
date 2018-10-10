package org.ozwillo.dcimporter.service

import org.ozwillo.dcimporter.config.DatacoreProperties
import org.ozwillo.dcimporter.model.BusinessAppConfiguration
import org.ozwillo.dcimporter.model.BusinessMapping
import org.ozwillo.dcimporter.model.datacore.DCOperator
import org.ozwillo.dcimporter.model.datacore.DCOrdering
import org.ozwillo.dcimporter.model.datacore.DCQueryParameters
import org.ozwillo.dcimporter.model.marchepublic.*
import org.ozwillo.dcimporter.repository.BusinessAppConfigurationRepository
import org.ozwillo.dcimporter.repository.BusinessMappingRepository
import org.ozwillo.dcimporter.util.*
import org.ozwillo.dcimporter.util.soap.response.parsing.ResponseObject
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.publisher.toFlux
import reactor.core.publisher.toMono
import java.lang.Exception
import java.lang.IllegalArgumentException
import java.time.Duration
import java.time.Instant

@Service
class MarcheSecuriseListingService(private val datacoreService: DatacoreService,
                                   private val datacoreProperties: DatacoreProperties,
                                   private val businessMappingRepository: BusinessMappingRepository,
                                   private val businessAppConfigurationRepository: BusinessAppConfigurationRepository){

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(MarcheSecuriseListingService::class.java)
    }

    private val startTime = Instant.now()
    private var triggeredError = 0

    private val MP_PROJECT = "marchepublic_0"

    @Value("\${marchesecurise.url.updateConsultation}")
    private val updateConsultationUrl = ""
    @Value("\${marchesecurise.url.registre}")
    private val registreUrl = ""

    fun refreshDatacoreRegistreReponse(){
        val ordre = Ordre.DATE_PREMIER_RETRAIT.toString()
        val sensOrdre = SensOrdre.ASC.toString()
        val startParam = 0
        val limitParam = Int.MAX_VALUE

        logger.debug("Beginning Datacore register update from Marchés Sécurisés ...")

        listOf(MSUtils.REPONSE_TYPE, MSUtils.RETRAIT_TYPE)
                .forEach {type ->
                    datacoreService.findResources(MP_PROJECT, MSUtils.CONSULTATION_TYPE, DCQueryParameters("mpconsultation:etat", DCOperator.EQ, DCOrdering.DESCENDING, Etat.PUBLISHED.toString()), startParam, limitParam)
                            .flatMap { consultation ->
                                val siret = consultation.getStringValue("mpconsultation:organization").substringAfterLast("/")

                                getRegistre(siret, type, consultation.getUri(), ordre, sensOrdre)
                            }
                            .flatMap {registre ->

                                val registreType = when(type){
                                    MSUtils.REPONSE_TYPE -> registre as RegistreReponse
                                    MSUtils.RETRAIT_TYPE -> registre as RegistreRetrait
                                    else -> registre
                                }

                                val dcRegistre = when(type){

                                    MSUtils.REPONSE_TYPE -> (registreType as RegistreReponse).toDcObject(datacoreProperties.baseUri, registreType.cle)

                                    MSUtils.RETRAIT_TYPE -> {

                                        val dcPersonne = (registreType.personne!!).toDcObject(datacoreProperties.baseUri)
                                        try {
                                            val currentResource = datacoreService.getResourceFromIRI(MP_PROJECT, MSUtils.PERSONNE_TYPE, registreType!!.personne!!.cle, null)

                                            if (currentResource.getStringValue("mppersonne:email") != registreType.personne!!.email
                                                    || currentResource.getStringValue("mppersonne:tel") != registreType.personne!!.telephone
                                                    || currentResource.getStringValue("mppersonne:fax") != registreType.personne!!.fax)
                                                datacoreService.updateResource(MP_PROJECT, MSUtils.PERSONNE_TYPE, dcPersonne, null)

                                        }catch (e: HttpClientErrorException){
                                            when(e.statusCode){
                                                HttpStatus.NOT_FOUND -> datacoreService.saveResource(MP_PROJECT, MSUtils.PERSONNE_TYPE, dcPersonne, null)
                                                else -> throw e
                                            }
                                        }

                                        (registreType as RegistreRetrait).toDcObject(datacoreProperties.baseUri, registreType.cle, registreType.personne!!.cle, registreType.pieceId)

                                    }
                                    else -> registreType.toDcObject(datacoreProperties.baseUri, registreType.cle!!)
                                }

                                try {
                                    datacoreService.getResourceFromIRI(MP_PROJECT, type, "FR/${registre.siret}/${registre.consultationReference}/${registreType.cle}", null)
                                    datacoreService.updateResource(MP_PROJECT, type, dcRegistre, null)
                                    logger.debug("Response register from Marchés Sécurisés FR/${registre.siret}/${registre.consultationReference}/${registreType.cle} updated in Datacore")
                                }catch (e: HttpClientErrorException){
                                    when(e.statusCode){
                                        HttpStatus.NOT_FOUND -> {
                                            datacoreService.saveResource(MP_PROJECT, type, dcRegistre, null)
                                            logger.debug("Response register from Marchés Sécurisés ${dcRegistre.getValues()} saved in Datacore")
                                        }
                                        else -> {
                                            triggeredError++
                                            logger.warn(e.message)
                                        }
                                    }
                                }
                                registre.toMono()
                            }
                            .onErrorResume {
                                logger.warn(it.message)
                                triggeredError++
                                it.toMono()
                            }
                            .doOnComplete {
                                val endTime = Instant.now()
                                logger.debug("Datacore register $type update end in ${(Duration.between(startTime, endTime)).toMillis()} ms with $triggeredError errors")
                            }.subscribe()
                }
    }

    fun getRegistre(siret:String, type: String, uri: String, ordre: String, sensOrdre: String): Flux<Registre> {
        var soapMessage: String

        val businessMappingMono: Mono<BusinessMapping> =
                businessMappingRepository.findByDcIdAndApplicationNameAndType(uri, MarcheSecuriseService.name, MSUtils.CONSULTATION_TYPE)
        val businessAppConfigurationMono: Mono<BusinessAppConfiguration> =
                businessAppConfigurationRepository.findByOrganizationSiretAndApplicationName(siret, MarcheSecuriseService.name)

        return businessMappingMono
                .zipWith(businessAppConfigurationMono)
                .flatMapMany { tuple2 ->
                    val businessMapping = tuple2.t1
                    val businessAppConfiguration = tuple2.t2

                    //Check or get Consultation msReference from Marchés Sécurisés
                    if (businessMapping.businessId2.isEmpty()) {
                        try {
                            saveConsultationMSReferenceFromMarcheSecurise(businessMapping, businessAppConfiguration, siret, uri)
                        } catch (e: Exception) {
                            logger.warn(e.message)
                        }
                    }

                    //Get registres from Marchés Sécurisés
                    var response = ""
                    soapMessage = when (type) {
                        MSUtils.REPONSE_TYPE -> MSUtils.generateListRegistreReponseRequest(businessAppConfiguration.login!!, businessAppConfiguration.password!!, businessMapping.businessId2, ordre, sensOrdre)
                        MSUtils.RETRAIT_TYPE -> MSUtils.generateListRegistreRetraitRequest(businessAppConfiguration.login!!, businessAppConfiguration.password!!, businessMapping.businessId2, ordre, sensOrdre)
                        else -> ""
                    }
                    if (!soapMessage.isEmpty()) {
                        response = MSUtils.sendSoap("${businessAppConfiguration.baseUrl}$registreUrl", soapMessage)
                    }

                    //Parse soap response to responseObject Flux
                    val responseObjects =
                            try {
                                MSUtils.parseToResponseObjectList(response, type, BindingKeyAction.UPDATE.value)
                            } catch (e: BadLogError) {
                                emptyList<ResponseObject>()
                            }

                    responseObjects.toFlux()
                }
                .flatMap { responseObject ->
                    when(type){
                        MSUtils.REPONSE_TYPE -> RegistreReponse.fromSoapObject(responseObject, siret, uri.substringAfterLast("/")).toMono()
                        MSUtils.RETRAIT_TYPE -> {
                            val clePiece = responseObject.properties!![1].value!!
                            businessMappingRepository.findByBusinessIdAndApplicationNameAndType(clePiece, MarcheSecuriseService.name, MSUtils.PIECE_TYPE)
                                    .flatMap {it ->
                                        RegistreRetrait.fromSoapObject(responseObject, siret, uri.substringAfterLast("/"), it.dcId).toMono()
                                    }
                        }
                        else -> {
                            throw SoapParsingUnexpectedError("Unable to recognize type")
                        }
                    }
                }
                .onErrorResume { e ->
                    when(e){
                        is IllegalArgumentException -> {
                            logger.warn("error on finding references from businessMapping, ${e.message}")
                            throw e
                        }
                        else -> {
                            logger.warn("Unexpected error")
                            throw e
                        }
                    }
                }
    }

    fun saveConsultationMSReferenceFromMarcheSecurise(businessMapping: BusinessMapping, businessAppConfiguration: BusinessAppConfiguration, siret: String, uri: String){
        var response: String = ""
        val soapMessage = MSUtils.generateReadConsultationRequest(businessAppConfiguration.login!!, businessAppConfiguration.password!!, businessAppConfiguration.instanceId!!, businessMapping.businessId)
        if (!soapMessage.isEmpty()){
            response = MSUtils.sendSoap("${businessAppConfiguration.baseUrl}$updateConsultationUrl", soapMessage)
        }
        val responseObject = MSUtils.parseToResponseType(response, MSUtils.CONSULTATION_TYPE, BindingKeyAction.GET.value)
        if (responseObject.properties!![0].name!! == "cle" && responseObject.properties!!.size >= 2){
            businessMapping.businessId2 = responseObject.properties!![1].value!!
            businessMappingRepository.save(BusinessMapping(id = businessMapping.id, dcId = businessMapping.dcId, businessId = businessMapping.businessId,
                    businessId2 = businessMapping.businessId2, applicationName = businessMapping.applicationName, type = businessMapping.type)).subscribe()
        }else if (responseObject.properties!![0].name!! == "load_pa_error"){
            throw BadPaError("Unable to process to consultation msReference reading from Marchés Sécurisés because of following error : bad pa")
        }else if (responseObject.properties!![0].name!! == "load_consultation_fail"){
            throw BadDceError("Unable to process to consultation msReference reading from Marchés Sécurisés because of following error : bad dce")
        }else{
            throw SoapParsingUnexpectedError("Unable to process to consultation msReference reading from Marchés Sécurisés because of unexpected error")
        }
    }
}