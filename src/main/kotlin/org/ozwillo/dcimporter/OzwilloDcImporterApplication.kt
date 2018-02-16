package org.ozwillo.dcimporter

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity


@SpringBootApplication
@EnableScheduling
@EnableWebSecurity
@ComponentScan(basePackages = ["org.oasis_eu.spring", "org.ozwillo.dcimporter"])
class OzwilloDcImporterApplication

fun main(args: Array<String>) {
    runApplication<OzwilloDcImporterApplication>(*args)
}
