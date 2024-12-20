package com.backend.naildp.config;

import java.security.GeneralSecurityException;
import java.security.Security;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import nl.martijndwars.webpush.PushAsyncService;

@Slf4j
@Getter
@Component
public class WebPushConfig {

	@Value("${vapid.public.key}")
	private String publicKey;
	@Value("${vapid.private.key}")
	private String privateKey;

	@Bean
	public BouncyCastleProvider bouncyCastleProvider() {
		BouncyCastleProvider bouncyCastleProvider = new BouncyCastleProvider();
		if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
			Security.addProvider(bouncyCastleProvider);
		}
		return bouncyCastleProvider;
	}

	@Bean
	@DependsOn("bouncyCastleProvider")
	public PushAsyncService pushAsyncService() {
		PushAsyncService pushAsyncService = null;
		try {
			pushAsyncService = new PushAsyncService(publicKey, privateKey);
			return pushAsyncService;
		} catch (GeneralSecurityException e) {
			log.error("errorMessage: {}", e.getMessage());
			throw new IllegalArgumentException("PushAsyncService 빈 등록 실패");
		}
	}

}