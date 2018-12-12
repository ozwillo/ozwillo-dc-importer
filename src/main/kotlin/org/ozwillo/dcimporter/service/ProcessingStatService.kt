package org.ozwillo.dcimporter.service

import org.ozwillo.dcimporter.model.ProcessingStat
import org.ozwillo.dcimporter.repository.ProcessingStatRepository
import org.ozwillo.dcimporter.util.BindingKeyAction
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
class ProcessingStatService (private val processingStatRepository: ProcessingStatRepository){

    fun create(processingStat: ProcessingStat){
        processingStatRepository.save(processingStat).subscribe()
    }

    fun getAll(): Flux<ProcessingStat> {
        return processingStatRepository.findAll()
    }

    //No use yet but just in case, it's here
    fun searchBy(date: String, model: String, organization: String, action: String): Flux<ProcessingStat> {

        return when {
            //date
            !date.isEmpty() && model.isEmpty() && organization.isEmpty() && action.isEmpty() ->
                getByDate(stringToLocalDateTime(date))
            //model
            date.isEmpty() && !model.isEmpty() && organization.isEmpty() && action.isEmpty() ->
                processingStatRepository.findByModel(model)
            //organization
            date.isEmpty() && model.isEmpty() && !organization.isEmpty() && action.isEmpty() ->
                processingStatRepository.findByOrganization(organization)
            //action
            date.isEmpty() && model.isEmpty() && organization.isEmpty() && !action.isEmpty() ->
                processingStatRepository.findByAction(action)
            //date+model
            !date.isEmpty() && !model.isEmpty() && organization.isEmpty() && action.isEmpty() ->
                processingStatRepository.findByModelAndCreationDateAfter(model, stringToLocalDateTime(date))
            //date+organization
            !date.isEmpty() && model.isEmpty() && !organization.isEmpty() && action.isEmpty() ->
                processingStatRepository.findByOrganizationAndCreationDateAfter(organization, stringToLocalDateTime(date))
            //date+action
            !date.isEmpty() && model.isEmpty() && organization.isEmpty() && !action.isEmpty() ->
                processingStatRepository.findByActionAndCreationDateAfter(action, stringToLocalDateTime(date))
            //date+model+organization
            !date.isEmpty() && !model.isEmpty() && !organization.isEmpty() && action.isEmpty() ->
                processingStatRepository.findByModelAndOrganizationAndCreationDateAfter(model, organization, stringToLocalDateTime(date))
            //date+model+action
            !date.isEmpty() && !model.isEmpty() && organization.isEmpty() && !action.isEmpty() ->
                processingStatRepository.findByModelAndActionAndCreationDateAfter(model, action, stringToLocalDateTime(date))
            //date+organization+action
            !date.isEmpty() && model.isEmpty() && !organization.isEmpty() && !action.isEmpty() ->
                processingStatRepository.findByOrganizationAndActionAndCreationDateAfter(organization, action, stringToLocalDateTime(date))
            //model+organization
            date.isEmpty() && !model.isEmpty() && !organization.isEmpty() && action.isEmpty() ->
                processingStatRepository.findByModelAndOrganization(model, organization)
            //model+action
            date.isEmpty() && !model.isEmpty() && organization.isEmpty() && !action.isEmpty() ->
                processingStatRepository.findByModelAndAction(model, action)
            //model+organization+action
            date.isEmpty() && !model.isEmpty() && !organization.isEmpty() && !action.isEmpty() ->
                processingStatRepository.findByModelAndOrganizationAndAction(model, organization, action)
            //organization+action
            date.isEmpty() && model.isEmpty() && !organization.isEmpty() && !action.isEmpty() ->
                processingStatRepository.findByOrganizationAndAction(organization, action)
            //model+organization+action+date
            !date.isEmpty() && !model.isEmpty() && !organization.isEmpty() && !action.isEmpty() ->
                processingStatRepository.findByModelAndOrganizationAndActionAndCreationDateAfter(model, organization, action, stringToLocalDateTime(date))
            else -> processingStatRepository.findAll()
        }
    }

    fun getByDate(date: LocalDateTime): Flux<ProcessingStat> {
        return processingStatRepository.findByCreationDateAfter(date)
    }

    fun getGeneralStat(date: String): Mono<ProcessingResumeFields>{

        val resumeByModel = mutableListOf<ProcessingResumeByModelFields>()
        val resumeByOrganization = mutableListOf<ProcessingResumeByOrganizationFields>()

        return getByDate(stringToLocalDateTime(date))
            .collectList()
            .map { processingList ->

                processingList.groupBy { processing -> processing.model }
                    .forEach { model, processingListByModel ->
                        resumeByModel.add(getModelResume(model, processingListByModel))
                    }

                processingList.groupBy { processing -> processing.organization }
                    .forEach { siret, processingListByOrganization ->

                        val modelResumeListForOrg = mutableListOf<ProcessingResumeByModelFields>()

                        processingListByOrganization.groupBy { p -> p.model }
                            .forEach { model, processingListByModelForOrg ->
                                modelResumeListForOrg.add(getModelResume(model, processingListByModelForOrg))
                            }

                        val totalCreated = processingListByOrganization.filter { p -> p.action == BindingKeyAction.CREATE.value }.count()
                        val totalDeleted = processingListByOrganization.filter { p -> p.action == BindingKeyAction.DELETE.value }.count()

                        resumeByOrganization.add(
                            ProcessingResumeByOrganizationFields(
                                orgSiret = siret,
                                nbreDistinctModel = processingListByOrganization.distinctBy { p -> p.model }.count(),
                                nbreOfCreation = totalCreated,
                                nbreOfUpdate = processingListByOrganization.filter { p -> (p.action == BindingKeyAction.UPDATE.value
                                        || p.action == BindingKeyAction.PUBLISH.value) }.count(),
                                nbreOfDeletion = totalDeleted,
                                nbreActiveModel = totalCreated - totalDeleted,
                                modelResume = modelResumeListForOrg
                            )
                        )
                    }
                ProcessingResumeFields(
                    nbreProcessing = processingList.count(),
                    nbreDistinctModel = processingList.distinctBy { processing -> processing.model }.count(),
                    nbreDistinctOrg = processingList.distinctBy { processing -> processing.organization }.count(),
                    resumeByModel = resumeByModel,
                    resumeByOrganization = resumeByOrganization
                )
            }
    }

    private fun getModelResume(model: String, processingList: List<ProcessingStat>): ProcessingResumeByModelFields {

        val created = processingList.filter { p -> p.action == BindingKeyAction.CREATE.value }.count()
        val deleted = processingList.filter { p -> p.action == BindingKeyAction.DELETE.value }.count()

        return ProcessingResumeByModelFields(
                    modelName = model,
                    nbreCreated = created,
                    nbreOfUpdate = processingList.filter { p -> (p.action == BindingKeyAction.UPDATE.value
                            || p.action == BindingKeyAction.PUBLISH.value) }.count(),
                    nbreDeleted = deleted,
                    nbreActive = created - deleted,
                    nbreOrganization = processingList.distinctBy {p -> p.organization }.count()
                )
    }

    private fun stringToLocalDateTime(date: String): LocalDateTime{
        val df = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSVV")
        return LocalDateTime.parse(date, df)
    }

}

data class ProcessingResumeFields(
    val nbreProcessing: Int,
    val nbreDistinctModel: Int,
    val nbreDistinctOrg: Int,
    val resumeByModel: MutableList<ProcessingResumeByModelFields>,
    val resumeByOrganization: MutableList<ProcessingResumeByOrganizationFields>
)

data class ProcessingResumeByModelFields(
    val modelName: String,
    val nbreCreated: Int,
    val nbreOfUpdate: Int,
    val nbreDeleted: Int,
    val nbreActive: Int,
    val nbreOrganization: Int
)

data class ProcessingResumeByOrganizationFields(
    val orgSiret: String,
    val nbreDistinctModel: Int,
    val nbreOfCreation: Int,
    val nbreOfUpdate: Int,
    val nbreOfDeletion: Int,
    val nbreActiveModel: Int,
    val modelResume: List<ProcessingResumeByModelFields>
)