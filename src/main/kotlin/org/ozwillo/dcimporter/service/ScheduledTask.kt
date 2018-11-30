package org.ozwillo.dcimporter.service

import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
@Profile("!test")
class ScheduledTask(private val marcheSecuriseListingService: MarcheSecuriseListingService) {

    @Scheduled(initialDelay = 3600000, fixedRate = 86400000)  //fixedRate 1/24H
    fun sheduledRefreshDatacoreRegistreReponse() {
        marcheSecuriseListingService.refreshDatacoreRegistreReponse()
    }

    @Scheduled(initialDelay = 3000000, fixedRate = 86400000)  //fixedRate 1/24H
    fun scheduledUpdateFirstHundredConsultationState() {
        marcheSecuriseListingService.updateFirstHundredPublishedConsultation().subscribe()
    }

}