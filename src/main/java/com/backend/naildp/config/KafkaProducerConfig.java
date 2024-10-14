package com.backend.naildp.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import com.backend.naildp.dto.chat.ChatMessageDto;
import com.backend.naildp.dto.chat.ChatUpdateDto;

@EnableKafka
@Configuration
public class KafkaProducerConfig {
	@Value("${spring.kafka.bootstrap-servers}")
	private String bootstrapAddress;

	@Bean
	public ProducerFactory<String, ChatMessageDto> producerFactory() {
		return new DefaultKafkaProducerFactory<>(producerConfigurations());
	}

	@Bean
	public Map<String, Object> producerConfigurations() {
		Map<String, Object> configurations = new HashMap<>();
		configurations.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
		configurations.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		configurations.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
		return configurations;
	}

	@Bean
	public KafkaTemplate<String, ChatMessageDto> kafkaTemplate() {
		return new KafkaTemplate<>(producerFactory());
	}

	@Bean
	public ProducerFactory<String, ChatUpdateDto> chatUpdateProducerFactory() {
		return new DefaultKafkaProducerFactory<>(producerConfigurations());
	}

	@Bean
	public KafkaTemplate<String, ChatUpdateDto> chatUpdateKafkaTemplate() {
		return new KafkaTemplate<>(chatUpdateProducerFactory());
	}
}
