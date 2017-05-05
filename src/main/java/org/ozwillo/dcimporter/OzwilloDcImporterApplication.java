package org.ozwillo.dcimporter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@ComponentScan(basePackages = {"org.oasis_eu.spring","org.ozwillo.dcimporter"})

public class OzwilloDcImporterApplication {

	
	public static void main(String[] args) {
		SpringApplication.run(OzwilloDcImporterApplication.class, args);

		
	}
}
