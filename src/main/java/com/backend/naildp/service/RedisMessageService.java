package com.backend.naildp.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Service;

import com.backend.naildp.dto.notification.PushNotificationDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisMessageService {

	private final RedisMessageListenerContainer redisMessageListenerContainer;
	private final RedisTemplate<String, PushNotificationDto> eventRedisTemplate;
	private final RedisSubscriber redisSubscriber;

	public void subscribe(String channel) {
		redisMessageListenerContainer.addMessageListener(redisSubscriber, ChannelTopic.of(channel));
	}

	public void removeSubscribe(String channel) {
		redisMessageListenerContainer.removeMessageListener(redisSubscriber, ChannelTopic.of(channel));
	}

	public void publish(String channel, PushNotificationDto pushNotificationDto) {
		log.info("channel = {}", channel);
		log.info("pushContent : {}", pushNotificationDto.getContent());
		eventRedisTemplate.convertAndSend(channel, pushNotificationDto);
	}

}
