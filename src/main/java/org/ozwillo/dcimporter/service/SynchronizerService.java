package org.ozwillo.dcimporter.service;

import java.util.Arrays;
import java.util.Optional;

import org.oasis_eu.spring.datacore.DatacoreClient;
import org.oasis_eu.spring.datacore.model.DCOperator;
import org.oasis_eu.spring.datacore.model.DCQueryParameters;
import org.oasis_eu.spring.datacore.model.DCResource;
import org.ozwillo.dcimporter.config.Prop;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class SynchronizerService implements CommandLineRunner {

	private static final Logger LOGGER = LoggerFactory.getLogger(SynchronizerService.class);

	private final SystemUserService systemUserService;
	private final DatacoreClient datacoreClient;
	private final PublikService publikService;
	private final Prop props;

	@Value("${publik.datacore.project}")
	private String datacoreProject;
	@Value("${publik.datacore.modelEM}")
	private String datacoreModelEM;
	@Value("${publik.datacore.modelSVE}")
	private String datacoreModelSVE;
	@Value("${publik.formTypeEM}")
	private String formTypeEM;
	@Value("${publik.formTypeSVE}")
	private String formTypeSVE;

	@Autowired
	public SynchronizerService(SystemUserService systemUserService, DatacoreClient datacoreClient,
			PublikService publikService, Prop props) {
		this.systemUserService = systemUserService;
		this.datacoreClient = datacoreClient;
		this.publikService = publikService;
		this.props = props;
	}

	public void run(String... args) {
		systemUserService.runAs(() -> {

			props.getInstance().forEach(instance -> {

                Optional<DCResource> dcOrg = publikService.getDCOrganization(instance.get("organization"));
				if (!dcOrg.isPresent()) {
				    LOGGER.error("Unable to find organization {}", instance.get("organization"));
                } else {
                    DCQueryParameters queryParameters =
                            new DCQueryParameters("citizenreq:organization", DCOperator.EQ, dcOrg.get().getUri());

                    Arrays.asList(datacoreModelEM, datacoreModelSVE).forEach(type -> {
                        if (datacoreClient.findResources(datacoreProject, type, queryParameters, 0, 1).isEmpty())
                            try {
                                if (type.equals(datacoreModelEM))
                                    publikService.syncPublikForms(instance.get("baseUrl"), dcOrg.get(), formTypeEM);
                                else if (type.equals(datacoreModelSVE))
                                    publikService.syncPublikForms(instance.get("baseUrl"), dcOrg.get(), formTypeSVE);
                                LOGGER.debug("Requests successfully synchronized");
                            } catch (Exception e) {
                                LOGGER.debug("Unable to synchronize past requests", e);
                            }
                        else
                            LOGGER.debug("Requests of type {} are already synchronized for {}", type, dcOrg.get().getUri());
                    });
                }
			});
		});
	}
}