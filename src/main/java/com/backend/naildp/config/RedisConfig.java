package com.backend.naildp.config;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisNode;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.backend.naildp.dto.notification.PushNotificationDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.lettuce.core.ReadFrom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// @Slf4j
@Configuration
@EnableCaching
// @RequiredArgsConstructor
public class RedisConfig {

	@Value("${spring.data.redis.host}")
	private String host;

	@Value("${spring.data.redis.port}")
	private int port;

	@Value("${spring.data.redis.password}")
	private String password;

	// private final RedisProperties redisProperties;

	@Bean
	public RedisConnectionFactory redisConnectionFactory() {
		RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
		configuration.setHostName(host);
		configuration.setPassword(password);
		configuration.setPort(port);
		return new LettuceConnectionFactory(configuration);
	}

	// @Bean
	// public RedisConnectionFactory redisConnectionFactory() {
	// 	LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
	// 		.readFrom(ReadFrom.REPLICA_PREFERRED)
	// 		.build();
	//
	// 	log.info("LettuceClientConfiguration build complete");
	//
	// 	RedisSentinelConfiguration sentinelConfig = new RedisSentinelConfiguration()
	// 		.master(redisProperties.getSentinel().getMaster());
	//
	// 	List<String> nodes = redisProperties.getSentinel().getNodes();
	// 	List<RedisNode> redisNodes = nodes.stream()
	// 		.map(node -> RedisNode.fromString(node + ":26379"))
	// 		.collect(Collectors.toList());
	//
	// 	log.info("RedisNodes: {}", redisNodes);
	// 	sentinelConfig.setSentinels(redisNodes);
	// 	sentinelConfig.setPassword(RedisPassword.of(redisProperties.getPassword()));
	//
	// 	log.info("RedisSentinelConfiguration build complete");
	//
	// 	return new LettuceConnectionFactory(sentinelConfig, clientConfig);
	// }

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
	public RedisTemplate<String, Long> stringLongRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
		RedisTemplate<String, Long> template = new RedisTemplate<>();

		template.setKeySerializer(new StringRedisSerializer());
		template.setValueSerializer(new GenericToStringSerializer<>(Long.class));

		template.setHashKeySerializer(new StringRedisSerializer());
		template.setHashValueSerializer(new GenericToStringSerializer<>(Long.class));

		template.setConnectionFactory(redisConnectionFactory);
		template.afterPropertiesSet();

		return template;
	}

	@Bean
	public RedisTemplate<String, Boolean> emptyFlagRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
		RedisTemplate<String, Boolean> template = new RedisTemplate<>();

		template.setKeySerializer(new StringRedisSerializer());
		template.setValueSerializer(new GenericToStringSerializer<>(Boolean.class));

		template.setHashKeySerializer(new StringRedisSerializer());
		template.setHashValueSerializer(new GenericToStringSerializer<>(Boolean.class));

		template.setConnectionFactory(redisConnectionFactory);
		template.afterPropertiesSet();

		return template;
	}

	/**
	 * 리펙토링
	 */
	@Bean
	public RedisTemplate<String, PushNotificationDto> eventRedisTemplate() {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());
		objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

		Jackson2JsonRedisSerializer<PushNotificationDto> jsonRedisSerializer = new Jackson2JsonRedisSerializer<>(
			objectMapper, PushNotificationDto.class);

		RedisTemplate<String, PushNotificationDto> eventRedisTemplate = new RedisTemplate<>();
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