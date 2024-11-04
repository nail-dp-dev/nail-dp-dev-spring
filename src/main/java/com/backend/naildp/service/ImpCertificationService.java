package com.backend.naildp.service;

import java.io.IOException;
import java.net.URI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.siot.IamportRestClient.IamportClient;
import com.siot.IamportRestClient.exception.IamportResponseException;
import com.siot.IamportRestClient.response.AccessToken;
import com.siot.IamportRestClient.response.Certification;
import com.siot.IamportRestClient.response.IamportResponse;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class ImpCertificationService {

	@Value("${portone.auth.api}")
	private String apiKey;

	@Value("${portone.auth.api_secret}")
	private String apiSecretKey;

	public String getImpAccessToken() {
		IamportClient client = new IamportClient(apiKey, apiSecretKey);
		try {
			IamportResponse<AccessToken> authResponse = client.getAuth();
			log.info("authResponse:{}", authResponse.getResponse().getToken());
			return authResponse.getResponse().getToken();
		} catch (IamportResponseException e) {
			log.info(e.getMessage());
			switch (e.getHttpStatusCode()) {
				case 401:
					//todo
					log.info("401");
					break;
				case 500:
					//todo
					log.info("500");
					break;
			}
		} catch (IOException e) {
			log.info("IOException : 서버 연결 실패");
		}
		return null;
	}

	public IamportResponse<Certification> certificate(String impUid) {
		IamportClient client = new IamportClient(apiKey, apiSecretKey);
		try {
			log.info("impUid:{}",impUid);
			IamportResponse<Certification> certificationIamportResponse = client.certificationByImpUid(impUid);
			log.info("certificationImportResponse : {}", certificationIamportResponse.getResponse());
			return certificationIamportResponse;
		} catch (IamportResponseException e) {
			log.info("IamResponseException:{}", e.getMessage());
			throw new RuntimeException(e.getMessage());
		} catch (IOException e) {
			log.info("IOException:{}", e.getMessage());
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}
	}
}
