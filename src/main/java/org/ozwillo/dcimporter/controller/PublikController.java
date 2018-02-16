package org.ozwillo.dcimporter.controller;

import org.ozwillo.dcimporter.model.publik.FormModel;
import org.ozwillo.dcimporter.service.PublikService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api")
public class PublikController {

	private static final Logger LOGGER = LoggerFactory.getLogger(PublikController.class);

	private final PublikService publikService;

	@Autowired
	public PublikController(PublikService publikService) {
		this.publikService = publikService;
	}

	@RequestMapping(value = "/form", method = RequestMethod.POST)
    public void getForm(@RequestBody FormModel form) {
		
		LOGGER.debug("formModel --> :"+form.toString());
		
		publikService.saveResourceToDC(form);
    }
}
