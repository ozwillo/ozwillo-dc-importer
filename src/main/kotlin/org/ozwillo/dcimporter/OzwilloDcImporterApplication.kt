package org.ozwillo.dcimporter

import org.springframework.boot.actuate.trace.http.HttpTraceRepository
import org.springframework.boot.actuate.trace.http.InMemoryHttpTraceRepository
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties
@ConfigurationPropertiesScan(basePackages = ["org.ozwillo.dcimporter.config"])
class OzwilloDcImporterApplication {

    @Bean
    fun httpTraceRepository(): HttpTraceRepository = InMemoryHttpTraceRepository()
}

fun main(args: Array<String>) {
    runApplication<OzwilloDcImporterApplication>(*args)
}
