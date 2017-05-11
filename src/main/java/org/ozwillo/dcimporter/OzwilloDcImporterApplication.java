package org.ozwillo.dcimporter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@ComponentScan(basePackages = {"org.oasis_eu.spring","org.ozwillo.dcimporter"})

public class OzwilloDcImporterApplication {

	private static final Logger LOGGER = LoggerFactory.getLogger(OzwilloDcImporterApplication.class);
	
	public static void main(String[] args) {
		SpringApplication.run(OzwilloDcImporterApplication.class, args);
		LOGGER.error("main--------------------------------------");
	}
}
