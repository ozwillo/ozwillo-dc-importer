package org.ozwillo.dcimporter.service

import org.ozwillo.dcimporter.config.DatacoreProperties
import org.ozwillo.dcimporter.model.BusinessAppConfiguration
import org.ozwillo.dcimporter.model.BusinessMapping
import org.ozwillo.dcimporter.model.datacore.DCBusinessResourceLight
import org.ozwillo.dcimporter.model.datacore.DCOperator
import org.ozwillo.dcimporter.model.datacore.DCOrdering
import org.ozwillo.dcimporter.model.datacore.DCQueryParameters
import org.ozwillo.dcimporter.model.marchepublic.Etat
import org.ozwillo.dcimporter.model.marchepublic.Ordre
import org.ozwillo.dcimporter.model.marchepublic.RegistreReponse
import org.ozwillo.dcimporter.model.marchepublic.SensOrdre
import org.ozwillo.dcimporter.repository.BusinessAppConfigurationRepository
import org.ozwillo.dcimporter.repository.BusinessMappingRepository
import org.ozwillo.dcimporter.util.BadLogError
import org.ozwillo.dcimporter.util.BindingKeyAction
import org.ozwillo.dcimporter.util.MSUtils
import org.ozwillo.dcimporter.util.SoapParsingUnexpectedError
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException
import reactor.core.publisher.Mono
import reactor.core.publisher.toMono

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

    @Scheduled(initialDelay = 30000, fixedRate = 86400000)  //fixedDelay 1/24H
    fun sheduledRefreshDatacoreRegistreReponse(){
        val ordre = Ordre.DATE_PREMIER_RETRAIT.toString()
        val sensOrdre = SensOrdre.ASC.toString()
        val startTime = System.currentTimeMillis()
        val startParam = 0
        var limitParam = 50
        var errorTriggered = 0

        logger.debug("Beginning Datacore register update from Marchés Sécurisés ...")
        lateinit var consultations: MutableList<DCBusinessResourceLight>
       do{
           try {
               consultations = datacoreService.findResources(MP_PROJECT, MSUtils.CONSULTATION_TYPE, DCQueryParameters("mpconsultation:etat", DCOperator.EQ, DCOrdering.DESCENDING, Etat.PUBLISHED.toString()), startParam, limitParam)
                       .collectList().block()!!
           }catch (e: Throwable){
               logger.warn(e.message)
               errorTriggered++
           }
           limitParam += 50

        } while (consultations.size == limitParam-50)

        //Register update for each consultation from DC
        consultations
                .forEach {consultation ->
                    val siret = consultation.getStringValue("mpconsultation:organization").substringAfterLast("/")
                    val reference = consultation.getIri().substringAfterLast("/")

                    try {
                        getRegistreReponse(siret, consultation.getUri(), ordre, sensOrdre)
                                .subscribe {registreReponses ->
                                    registreReponses
                                            .onEach {registreReponse ->
                                                val dcRegistreReponse = registreReponse.toDcObject(datacoreProperties.baseUri, siret, reference, registreReponse.cleReponse)
                                                try {
                                                    datacoreService.getResourceFromIRI(MP_PROJECT, MSUtils.RESPONSE_TYPE, "FR/$siret/$reference/${registreReponse.cleReponse}", null)
                                                    datacoreService.updateResource(MP_PROJECT, MSUtils.RESPONSE_TYPE, dcRegistreReponse, null)
                                                }catch (e: HttpClientErrorException){
                                                    when(e.statusCode){
                                                        HttpStatus.NOT_FOUND -> {
                                                            datacoreService.saveResource(MP_PROJECT, MSUtils.RESPONSE_TYPE, dcRegistreReponse, null)
                                                        }
                                                        else -> {
                                                            logger.warn(e.message)
                                                            errorTriggered++
                                                        }
                                                    }
                                                }
                                            }
                                    val endTime = System.currentTimeMillis()
                                    logger.debug("... Datacore register update complete in ${endTime - startTime}ms with $errorTriggered error")
                                }
                    }catch (e: BadLogError){
                        logger.warn(e.message)
                        errorTriggered++
                    }catch (e: SoapParsingUnexpectedError){
                        logger.warn(e.message)
                        errorTriggered++
                    }catch (e: IllegalArgumentException){
                        logger.warn(e.message)
                        errorTriggered++
                    }
                }

    }

    fun getRegistreReponse(siret:String, uri: String, ordre: String, sensOrdre: String): Mono<List<RegistreReponse>> {
        val registreList: MutableList<RegistreReponse> = mutableListOf()
        return try {
            businessMappingRepository.findByDcIdAndApplicationNameAndType(uri, MarcheSecuriseService.name, MSUtils.CONSULTATION_TYPE)
                    .flatMap{ businessMapping ->
                        businessAppConfigurationRepository.findByOrganizationSiretAndApplicationName(siret, MarcheSecuriseService.name)
                                .flatMap {
                                    if (businessMapping.businessId2.isEmpty()) {
                                        saveConsultationMSReferenceFromMarcheSecurise(businessMapping, it, siret, uri)
                                    }
                                    it.toMono()
                                }
                                .map {
                                    var response: String = ""
                                    val soapMessage = MSUtils.generateListElecResponseRequest(it.login!!, it.password!!, businessMapping.businessId2, ordre, sensOrdre)
                                    if (!soapMessage.isEmpty()){
                                        response = MSUtils.sendSoap("${it.baseUrl}/$registreUrl", soapMessage)
                                    }
                                    MSUtils.parseToResponseObjectList(response, MSUtils.RESPONSE_TYPE, BindingKeyAction.UPDATE.value)
                                            .forEach { responseObject ->
                                                val registreReponse = RegistreReponse.fromSoapObject(responseObject)
                                                registreList.add(registreReponse)
                                            }
                                    registreList as List<RegistreReponse>
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
            response = MSUtils.sendSoap("${businessAppConfiguration.baseUrl}/$updateConsultationUrl", soapMessage)
        }
        val responseObject = MSUtils.parseToResponseType(response, MSUtils.CONSULTATION_TYPE, BindingKeyAction.GET.value)
        businessMapping.businessId2 = responseObject.properties!![1].value!!
        businessMappingRepository.save(BusinessMapping(id = businessMapping.id, dcId = businessMapping.dcId, businessId = businessMapping.businessId,
                businessId2 = businessMapping.businessId2, applicationName = businessMapping.applicationName, type = businessMapping.type)).subscribe()
    }
}