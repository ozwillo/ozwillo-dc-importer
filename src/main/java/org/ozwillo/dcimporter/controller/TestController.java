package org.ozwillo.dcimporter.controller;


import org.ozwillo.dcimporter.service.PublikService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

	@Autowired
	private PublikService formService;
	
	@RequestMapping(value = "/test")
	public void test(){
		
		//formService.sign_url(null, null, null);
		formService.getListForms();
		formService.getForm("http://localhost:8080/api/forms/form");
		//formService.calculateSignature();
		formService.getPublikListForms();

	}
}
