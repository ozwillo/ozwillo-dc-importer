package org.ozwillo.dcimporter.controller;


import org.ozwillo.dcimporter.model.ListFormsModel;
import org.ozwillo.dcimporter.service.FormService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

	@Autowired
	private FormService formService;
	
	@RequestMapping(value = "/test")
	public void test(){
		
		ListFormsModel[] forms = formService.getListForms();

	}
}
