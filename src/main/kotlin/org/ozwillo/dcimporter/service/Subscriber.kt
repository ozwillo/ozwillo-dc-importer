package org.ozwillo.dcimporter.service

import org.oasis_eu.spring.datacore.model.DCResource
import reactor.core.publisher.Mono

interface Subscriber {

    fun getName(): String
    fun onNewData(dcResource: DCResource): Mono<String>
}