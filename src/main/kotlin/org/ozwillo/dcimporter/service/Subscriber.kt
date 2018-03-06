package org.ozwillo.dcimporter.service

import org.ozwillo.dcimporter.model.datacore.DCBusinessResourceLight
import org.ozwillo.dcimporter.model.datacore.DCResource
import reactor.core.publisher.Mono

interface Subscriber {

    fun getName(): String
    fun onNewData(dcResource: DCBusinessResourceLight): Mono<String>
}