package org.ozwillo.dcimporter

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling


@SpringBootApplication
@EnableScheduling
class OzwilloDcImporterApplication

fun main(args: Array<String>) {
    runApplication<OzwilloDcImporterApplication>(*args)
}
