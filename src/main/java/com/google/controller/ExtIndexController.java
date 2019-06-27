package com.google.controller;

import javax.servlet.http.HttpServletRequest;

import com.google.extspringmvc.extannotation.ExtController;
import com.google.extspringmvc.extannotation.ExtRequestMapping;

@ExtController
@ExtRequestMapping(value="/index")
public class ExtIndexController {
	
	@ExtRequestMapping(value="/test")
	public String test(HttpServletRequest req) {
		System.out.println(" ÷–¥springmvc...");
		req.setAttribute("name","wuyifan");
		return "index";
	}

}
