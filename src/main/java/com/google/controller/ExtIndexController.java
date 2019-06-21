package com.google.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.extspringmvc.extannotation.ExtController;
import com.google.extspringmvc.extannotation.ExtRequestMapping;

@ExtController
@ExtRequestMapping("/index")
public class ExtIndexController {
	
	@ExtRequestMapping("/test")
	public String test(HttpServletRequest req) {
		System.out.println(" ÷–¥springmvc...");
		req.setAttribute("name","wuyifan");
		return "index";
	}

}
