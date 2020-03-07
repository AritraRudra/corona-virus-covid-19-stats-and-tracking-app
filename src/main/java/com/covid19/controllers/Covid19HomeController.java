package com.covid19.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

@Controller
public class Covid19HomeController {

	public String infectedInfo(Model uiModel) {
		return "infectedInfo";
	}

}
