package com.backend.naildp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.backend.naildp.service.ImportRestService;

import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/api/fo/import")
public class ImportRestController {

	@Value("${project.import.rest-api-key}")
	private String api_key;

	@Value("${project.import.rest-api-secret-key}")
	private String api_secret;

	private final ImportRestService importRestService;

	@ResponseBody
	@GetMapping("/certification-value")
	public String getCertificationValue() {
		return importRestService.getCertificationValue();
	}
}
