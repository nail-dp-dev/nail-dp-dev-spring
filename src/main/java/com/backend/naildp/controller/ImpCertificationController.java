package com.backend.naildp.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.backend.naildp.dto.auth.ImpCertificationRequestDto;
import com.backend.naildp.service.ImpCertificationService;
import com.siot.IamportRestClient.response.Certification;
import com.siot.IamportRestClient.response.IamportResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/api/portone")
public class ImpCertificationController {

	private final ImpCertificationService impCertificationService;

	@ResponseBody
	@PostMapping("/certification")
	public ResponseEntity<?> passCertificate(@RequestBody ImpCertificationRequestDto impCertificationRequestDto) {
		IamportResponse<Certification> certificate = impCertificationService.certificate(impCertificationRequestDto.getImp_uid());
		return ResponseEntity.ok(certificate.getResponse());
	}

	@ResponseBody
	@GetMapping("/token")
	public ResponseEntity<?> impAccessToken() {
		String impAccessToken = impCertificationService.getImpAccessToken();
		return ResponseEntity.ok(impAccessToken);
	}

	// @GetMapping
	// public RedirectView examplePage() {
	// 	return new RedirectView("/certificate.html"); // /static/example.html에 있는 정적 파일로 리다이렉트
	// }
}
