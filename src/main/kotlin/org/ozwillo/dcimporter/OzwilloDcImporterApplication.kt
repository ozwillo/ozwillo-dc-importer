package org.ozwillo.dcimporter

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity


@SpringBootApplication
@EnableScheduling
// Disabled for now, re-add later   //TODO: To re-think since we introduce @EnableWebFluxSecurity and a web security config class
//@EnableWebSecurity
class OzwilloDcImporterApplication

fun main(args: Array<String>) {
    runApplication<OzwilloDcImporterApplication>(*args)
}
