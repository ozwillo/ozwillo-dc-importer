package org.ozwillo.dcimporter

import org.ozwillo.dcimporter.config.ApplicationProperties
import org.ozwillo.dcimporter.config.DatacoreProperties
import org.ozwillo.dcimporter.config.KernelProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling


@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(ApplicationProperties::class, DatacoreProperties::class, KernelProperties::class)
class OzwilloDcImporterApplication

fun main(args: Array<String>) {
    runApplication<OzwilloDcImporterApplication>(*args)
}
