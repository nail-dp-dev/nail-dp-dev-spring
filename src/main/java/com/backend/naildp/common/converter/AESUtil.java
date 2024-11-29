package com.backend.naildp.common.converter;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class AESUtil {

	private static final Charset ENCODING_TYPE = StandardCharsets.UTF_8;
	private static final String PADDING_ALGORITHM = "AES/CBC/PKCS5Padding";

	@Value("${aes.secret-key}")
	private String secretKey;
	@Value("${aes.iv-key}")
	private String ivKey;

	private IvParameterSpec ivParameterSpec;
	private SecretKeySpec secretKeySpec;


	@PostConstruct
	public void init() {
		this.ivParameterSpec = new IvParameterSpec(ivKey.getBytes());
		this.secretKeySpec = new SecretKeySpec(secretKey.getBytes(ENCODING_TYPE), "AES");
	}

	public String encrypt(String plainText) {
		try {
			Cipher cipher = Cipher.getInstance(PADDING_ALGORITHM);
			cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);
			byte[] encrypted = cipher.doFinal(plainText.getBytes(ENCODING_TYPE));
			return Base64.getEncoder().encodeToString(encrypted);
		} catch (Exception e) {
			log.error("error {}: {}", e.getClass(), e.getMessage());
			throw new IllegalArgumentException("encrypt 실패", e);
		}
	}

	public String decrypt(String cipherText) {
		try {
			Cipher cipher = Cipher.getInstance(PADDING_ALGORITHM);
			cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);
			byte[] decoded = Base64.getDecoder().decode(cipherText);
			return new String(cipher.doFinal(decoded), ENCODING_TYPE);
		} catch (Exception e) {
			log.error("error {}: {}", e.getClass(), e.getMessage());
			throw new IllegalArgumentException("decrypt 실패", e);
		}
	}

}
