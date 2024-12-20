package com.backend.naildp.config.chat;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import com.backend.naildp.dto.chat.ChatMessageDto;
import com.backend.naildp.dto.chat.ChatUpdateDto;
import com.backend.naildp.dto.chat.TempRoomSwitchDto;

@EnableKafka
@Configuration
public class KafkaConsumerConfig {
	@Value("${spring.kafka.bootstrap-servers}")
	private String bootstrapAddress;

	@Value("${KAFKA_GROUP_ID:default-group}")
	private String groupId;

	@Bean
	public ConsumerFactory<String, ChatMessageDto> consumerFactory() {
		return new DefaultKafkaConsumerFactory<>(consumerConfigurations(), new StringDeserializer(),
			new JsonDeserializer<>(ChatMessageDto.class));
	}

	private Map<String, Object> consumerConfigurations() {
		Map<String, Object> configurations = new HashMap<>();
		configurations.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
		configurations.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
		configurations.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		configurations.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
		configurations.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
		configurations.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest"); // earliest: 전체 , latest: 최신 메시지
		return configurations;
	}

	// 멀티쓰레드에 대한 동기화 제공하는 컨슈머를 생산하기 위한 Factory
	@Bean
	public ConcurrentKafkaListenerContainerFactory<String, ChatMessageDto> kafkaListenerContainerFactory() {
		ConcurrentKafkaListenerContainerFactory<String, ChatMessageDto> factory = new ConcurrentKafkaListenerContainerFactory<>();
		factory.setConsumerFactory(consumerFactory());
		return factory;
	}

	@Bean
	public ConsumerFactory<String, ChatUpdateDto> chatUpdateConsumerFactory() {
		return new DefaultKafkaConsumerFactory<>(consumerConfigurations(), new StringDeserializer(),
			new JsonDeserializer<>(ChatUpdateDto.class));
	}

	@Bean
	public ConcurrentKafkaListenerContainerFactory<String, ChatUpdateDto> chatUpdateKafkaListenerContainerFactory() {
		ConcurrentKafkaListenerContainerFactory<String, ChatUpdateDto> factory = new ConcurrentKafkaListenerContainerFactory<>();
		factory.setConsumerFactory(chatUpdateConsumerFactory());
		return factory;
	}

	@Bean
	public ConsumerFactory<String, TempRoomSwitchDto> chatRoomSwitchConsumerFactory() {
		return new DefaultKafkaConsumerFactory<>(consumerConfigurations(), new StringDeserializer(),
			new JsonDeserializer<>(TempRoomSwitchDto.class));
	}

	@Bean
	public ConcurrentKafkaListenerContainerFactory<String, TempRoomSwitchDto> chatRoomSwitchKafkaListenerContainerFactory() {
		ConcurrentKafkaListenerContainerFactory<String, TempRoomSwitchDto> factory = new ConcurrentKafkaListenerContainerFactory<>();
		factory.setConsumerFactory(chatRoomSwitchConsumerFactory());
		return factory;
	}
}