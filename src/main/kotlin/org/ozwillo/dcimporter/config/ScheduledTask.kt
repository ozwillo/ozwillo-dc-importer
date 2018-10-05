package org.ozwillo.dcimporter.config

import org.ozwillo.dcimporter.service.MarcheSecuriseListingService
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
@Profile("dev")
class ScheduledTask(private val marcheSecuriseListingService: MarcheSecuriseListingService){

    @Scheduled(initialDelay = 30000, fixedRate = 86400000)  //fixedDelay 1/24H
    fun sheduledRefreshDatacoreRegistreReponse(){
        marcheSecuriseListingService.refreshDatacoreRegistreReponse()
    }

}