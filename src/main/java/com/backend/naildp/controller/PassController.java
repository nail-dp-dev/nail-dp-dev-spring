package com.backend.naildp.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.backend.naildp.service.PassCertificateService;
import com.siot.IamportRestClient.response.Certification;
import com.siot.IamportRestClient.response.IamportResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/portone")
public class PassController {

	private final PassCertificateService passCertificateService;

	// @ResponseBody
	@GetMapping("/imp/{imp_uid}")
	public ResponseEntity<?> passCertificate(@PathVariable("imp_uid") String impUid) {
		IamportResponse<Certification> certificate = passCertificateService.certificate(impUid);
		return ResponseEntity.ok(certificate.getResponse());
	}

	@GetMapping("/imp/token")
	public ResponseEntity<?> impAccessToken() {
		String impAccessToken = passCertificateService.getImpAccessToken();
		return ResponseEntity.ok(impAccessToken);
	}
}
