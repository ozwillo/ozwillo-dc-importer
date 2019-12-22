package org.ozwillo.dcimporter

import org.ozwillo.dcimporter.config.ApplicationProperties
import org.ozwillo.dcimporter.config.DatacoreProperties
import org.ozwillo.dcimporter.config.InseeSireneProperties
import org.ozwillo.dcimporter.config.KernelProperties
import org.springframework.boot.actuate.trace.http.HttpTraceRepository
import org.springframework.boot.actuate.trace.http.InMemoryHttpTraceRepository
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(ApplicationProperties::class, DatacoreProperties::class, KernelProperties::class, InseeSireneProperties::class)
class OzwilloDcImporterApplication {

    @Bean
    fun httpTraceRepository(): HttpTraceRepository = InMemoryHttpTraceRepository()
}

fun main(args: Array<String>) {
    runApplication<OzwilloDcImporterApplication>(*args)
}
