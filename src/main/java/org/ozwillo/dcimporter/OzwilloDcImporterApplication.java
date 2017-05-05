package org.ozwillo.dcimporter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "org.oasis_eu.spring")
public class OzwilloDcImporterApplication {

	public static void main(String[] args) {
		SpringApplication.run(OzwilloDcImporterApplication.class, args);
	}
}
