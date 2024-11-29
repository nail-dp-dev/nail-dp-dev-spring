package com.backend.naildp.common.converter;

import org.springframework.util.StringUtils;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.RequiredArgsConstructor;

@Converter
@RequiredArgsConstructor
public class StringCryptoConverter implements AttributeConverter<String, String> {

	private final AESUtil aesUtil;

	@Override
	public String convertToDatabaseColumn(String attribute) {
		if (StringUtils.hasText(attribute)) {
			return aesUtil.encrypt(attribute);
		}
		return "no String";
	}

	@Override
	public String convertToEntityAttribute(String dbData) {
		if (StringUtils.hasText(dbData)) {
			return aesUtil.decrypt(dbData);
		}
		return "no saved String";
	}
}
