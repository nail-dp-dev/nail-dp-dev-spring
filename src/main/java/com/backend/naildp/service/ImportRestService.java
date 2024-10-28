package com.backend.naildp.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class ImportRestService {

	private final CertificationValueRepository certificationValueRepository;

	@Transactional(rollbackFor = Exception.class)
	public String getCertificationValue() {
		new CertificationValues();
	}
}
