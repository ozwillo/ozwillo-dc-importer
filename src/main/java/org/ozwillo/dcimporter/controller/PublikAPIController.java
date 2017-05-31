package org.ozwillo.dcimporter.controller;

import org.oasis_eu.spring.datacore.DatacoreClient;
import org.oasis_eu.spring.datacore.model.DCResource;
import org.ozwillo.dcimporter.model.FormModel;
import org.ozwillo.dcimporter.service.PublikService;
import org.ozwillo.dcimporter.service.SystemUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api")
public class PublikAPIController {

	private static final Logger LOGGER = LoggerFactory.getLogger(PublikAPIController.class);
	
	@Value("${publik.datacore.project}")
    private String datacoreProject;
	
	@Autowired
	PublikService publikService;
	
	private final DatacoreClient datacoreClient;
    private final SystemUserService systemUserService;
	
    
    @Autowired
    public PublikAPIController(DatacoreClient datacoreClient, SystemUserService systemUserService) {
        this.datacoreClient = datacoreClient;
        this.systemUserService = systemUserService;
    }
    
	@RequestMapping(value = "/form/", method = RequestMethod.POST)
    public void getForm(@RequestBody FormModel form) {
		
		LOGGER.debug("formModel --> :"+form.toString());
		
		DCResource dcResource = publikService.convertToDCResource(form);
		LOGGER.debug("DCResource --> "+dcResource.toString());
		systemUserService.runAs(() ->
        datacoreClient.saveResource(datacoreProject, dcResource)
		);


    }
}
