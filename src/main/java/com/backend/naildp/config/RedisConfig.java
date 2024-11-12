package com.backend.naildp.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.backend.naildp.dto.PushNotificationResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Configuration
@EnableCaching
public class RedisConfig {

	@Value("${spring.data.redis.host}")
	private String host;

	@Value("${spring.data.redis.port}")
	private int port;

	@Value("${spring.data.redis.password}")
	private String password;

	@Bean
	public RedisConnectionFactory redisConnectionFactory() {
		RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
		configuration.setHostName(host);
		configuration.setPassword(password);
		configuration.setPort(port);
		return new LettuceConnectionFactory(configuration);
	}

	@Bean
	@Primary
	public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
		RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
		redisTemplate.setKeySerializer(new StringRedisSerializer());
		redisTemplate.setValueSerializer(new StringRedisSerializer());
		redisTemplate.setConnectionFactory(redisConnectionFactory);
		return redisTemplate;
	}

	@Bean
	public RedisOperations<String, PushNotificationResponseDto> eventRedisOperations() {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());
		objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

		Jackson2JsonRedisSerializer<PushNotificationResponseDto> jsonRedisSerializer = new Jackson2JsonRedisSerializer<>(
			objectMapper, PushNotificationResponseDto.class);

		RedisTemplate<String, PushNotificationResponseDto> eventRedisTemplate = new RedisTemplate<>();
		eventRedisTemplate.setConnectionFactory(redisConnectionFactory());
		eventRedisTemplate.setKeySerializer(RedisSerializer.string());
		eventRedisTemplate.setValueSerializer(jsonRedisSerializer);
		eventRedisTemplate.setHashKeySerializer(RedisSerializer.string());
		eventRedisTemplate.setHashValueSerializer(jsonRedisSerializer);
		return eventRedisTemplate;
	}

	@Bean
	public RedisMessageListenerContainer redisMessageListenerContainer() {
		RedisMessageListenerContainer redisMessageListenerContainer = new RedisMessageListenerContainer();
		redisMessageListenerContainer.setConnectionFactory(redisConnectionFactory());
		return redisMessageListenerContainer;
	}

	@Bean
	public RedisTemplate<String, Object> chatRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
		RedisTemplate<String, Object> chatRedisTemplate = new RedisTemplate<>();
		chatRedisTemplate.setConnectionFactory(redisConnectionFactory);
		chatRedisTemplate.setKeySerializer(new StringRedisSerializer());
		chatRedisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
		return chatRedisTemplate;
	}

}