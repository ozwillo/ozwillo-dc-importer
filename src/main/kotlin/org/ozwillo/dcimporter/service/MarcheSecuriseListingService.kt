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
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException
import reactor.core.publisher.Mono
import reactor.core.publisher.toMono
import java.lang.Exception

@Service
class MarcheSecuriseListingService(private val datacoreService: DatacoreService,
                                   private val datacoreProperties: DatacoreProperties,
                                   private val businessMappingRepository: BusinessMappingRepository,
                                   private val businessAppConfigurationRepository: BusinessAppConfigurationRepository){

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(MarcheSecuriseListingService::class.java)
    }

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
        var type = ""

        logger.debug("Beginning Datacore register update from Marchés Sécurisés ...")
        datacoreService.findResources(MP_PROJECT, MSUtils.CONSULTATION_TYPE, DCQueryParameters("mpconsultation:etat", DCOperator.EQ, DCOrdering.DESCENDING, Etat.PUBLISHED.toString()), startParam, limitParam)
                .flatMap { consultation ->
                    val siret = consultation.getStringValue("mpconsultation:organization").substringAfterLast("/")
                    val reference = consultation.getIri().substringAfterLast("/")

                    //TODO: add a type list and an other loop
                    type = MSUtils.RESPONSE_TYPE    //TODO: Will become a list of register type

                    getRegistre(siret, type, consultation.getUri(), ordre, sensOrdre)
                }
                .flatMap {registreReponses ->
                                registreReponses
                                        .onEach {registre ->

                                            val registreType = when(type){
                                               MSUtils.RESPONSE_TYPE -> registre as RegistreReponse
                                                else -> registre
                                            }

                                            val dcRegistre = registreType.toDcObject(datacoreProperties.baseUri, registre.siret!!, registre.consultationReference!!, registreType.cle!!)

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
                                                        logger.warn(e.message)
                                                    }
                                                }
                                            }
                                        }
                                registreReponses.toMono()
                }.subscribe()
    }

    fun getRegistre(siret:String, type: String, uri: String, ordre: String, sensOrdre: String): Mono<List<Registre>> {
        val registreList: MutableList<RegistreReponse> = mutableListOf()
        var soapMessage: String

        return try {
            businessMappingRepository.findByDcIdAndApplicationNameAndType(uri, MarcheSecuriseService.name, MSUtils.CONSULTATION_TYPE)
                    .flatMap{ businessMapping ->
                        businessAppConfigurationRepository.findByOrganizationSiretAndApplicationName(siret, MarcheSecuriseService.name)
                                .flatMap {
                                    if (businessMapping.businessId2.isEmpty()) {
                                        try {
                                            saveConsultationMSReferenceFromMarcheSecurise(businessMapping, it, siret, uri)
                                        }catch (e: Exception){
                                            logger.warn(e.message)
                                        }
                                    }
                                    it.toMono()
                                }
                                .flatMap {
                                    var response: String = ""
                                    soapMessage = when(type){
                                        MSUtils.RESPONSE_TYPE -> MSUtils.generateListElecResponseRequest(it.login!!, it.password!!, businessMapping.businessId2, ordre, sensOrdre)
                                        else -> ""
                                    }
                                    if (!soapMessage.isEmpty()){
                                        response = MSUtils.sendSoap("${it.baseUrl}$registreUrl", soapMessage)
                                    }
                                    try {
                                        MSUtils.parseToResponseObjectList(response, type, BindingKeyAction.UPDATE.value)
                                                .forEach { responseObject ->
                                                    when(type){
                                                        MSUtils.RESPONSE_TYPE -> {
                                                            val registreReponse = RegistreReponse.fromSoapObject(responseObject, siret, uri.substringAfterLast("/"))
                                                            registreList.add(registreReponse)
                                                        }
                                                    }

                                                }
                                        when(type){
                                            MSUtils.RESPONSE_TYPE -> (registreList as List<RegistreReponse>).toMono()
                                            else -> {
                                                logger.warn("Unable to recognize type (reponse ou retrait)")
                                                emptyList<Registre>().toMono()
                                            }
                                        }

                                    }catch (e: BadLogError){
                                        emptyList<Registre>().toMono()
                                    }
                                }
                    }
        }catch (e: IllegalArgumentException){
            logger.warn("error on finding consultation msReference from businessMapping, ${e.message}")
            throw e
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