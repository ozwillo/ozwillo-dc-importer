package org.ozwillo.dcimporter.controller;

import org.ozwillo.dcimporter.model.FormModel;
import org.ozwillo.dcimporter.service.PublikService;
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
<<<<<<< ec23f31637ad746193dd5bc533fbaf9c4737d2c5

	@RequestMapping(value = "/form/", method = RequestMethod.POST)
	public void getForm(@RequestBody FormModel form) {

		LOGGER.debug("formModel --> :" + form.toString());

		publikService.saveResourceToDC(form);
	}
=======
	
    
	@RequestMapping(value = "/form/", method = RequestMethod.POST)
    public void getForm(@RequestBody FormModel form) {
		
		LOGGER.debug("formModel --> :"+form.toString());
		
		publikService.saveResourceToDC(form);
		
    }
>>>>>>> Add support for multiple Publik instances and organization
}
