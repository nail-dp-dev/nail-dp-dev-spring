package com.backend.naildp.service;

import java.util.concurrent.CompletableFuture;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import com.backend.naildp.dto.chat.ChatMessageDto;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaProducerService {

	private static final String TOPIC_NAME = "chatting";

	private final KafkaTemplate<String, ChatMessageDto> kafkaTemplate;

	private final ObjectMapper objectMapper = new ObjectMapper();

	public void send(ChatMessageDto chatMessageDto) {

		CompletableFuture<SendResult<String, ChatMessageDto>> future = kafkaTemplate.send(TOPIC_NAME, chatMessageDto);

		future.whenComplete((result, ex) -> {
			if (ex != null) {
				// 전송 실패 처리
				log.error("Unable to send message=[" + chatMessageDto.getContent() + "] due to : " + ex.getMessage());
			} else {
				// 전송 성공 처리
				log.info("채팅방: {}, 보낸사람: {}, 메시지: {}", chatMessageDto.getChatRoomId(), chatMessageDto.getSender(),
					chatMessageDto.getContent());
			}
		});
	}
}
