package com.backend.naildp.service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Component;

import com.backend.naildp.dto.chat.ChatMessageDto;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class KafkaConsumerService {

	private static final String TOPIC_NAME = "chatting";

	private final SimpMessageSendingOperations template;

	ObjectMapper objectMapper = new ObjectMapper();

	@KafkaListener(topics = TOPIC_NAME)
	public void listenMessage(ChatMessageDto chatMessageDto) {
		try {
			template.convertAndSend("/sub/chat/" + chatMessageDto.getChatRoomId(), chatMessageDto);

		} catch (Exception e) {
			throw new RuntimeException("예외 발생 : " + e.getMessage());
		}
	}
}