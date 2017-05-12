package org.ozwillo.dcimporter.service;


import java.util.List;

import org.oasis_eu.spring.datacore.DatacoreClient;
import org.oasis_eu.spring.datacore.model.DCResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;


@Service
public class SynchronizerService {


	private static final Logger LOGGER = LoggerFactory.getLogger(SynchronizerService.class);
	
    @Autowired
    private SystemUserService systemUserService;

    @Autowired
	private DatacoreClient datacoreClient;

    @Scheduled(fixedDelayString = "${application.syncDelay}")
    public void synchronizeOrgs() {
        
    	systemUserService.runAs(() -> {
    		
    		LOGGER.info("findRessources");
    		
    		List<DCResource> dcRessources = datacoreClient.findResources("oasis.main","geo:Area_0");
    		
    		LOGGER.info("findRessources "+dcRessources.toString());
    		
    	});
                
        
    }

}