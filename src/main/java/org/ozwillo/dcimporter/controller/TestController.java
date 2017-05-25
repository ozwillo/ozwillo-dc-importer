package org.ozwillo.dcimporter.controller;


import java.net.MalformedURLException;
import java.net.URISyntaxException;

import org.ozwillo.dcimporter.service.PublikService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {


	@Autowired
	private PublikService publikService;
	
	@RequestMapping(value = "/test")
	public void test() throws URISyntaxException, MalformedURLException{
		
		publikService.syncPublikForms();

	}
}
