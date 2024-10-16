package com.backend.naildp.service.chat;

import java.util.List;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Component;

import com.backend.naildp.dto.chat.ChatMessageDto;
import com.backend.naildp.dto.chat.ChatUpdateDto;
import com.backend.naildp.repository.ChatRoomUserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaConsumerService {

	private static final String TOPIC_NAME = "chatting";
	private static final String CHAT_UPDATE_TOPIC = "chatUpdates";

	private final SimpMessageSendingOperations template;
	private final ChatRoomUserRepository chatRoomUserRepository;

	ObjectMapper objectMapper = new ObjectMapper();

	@KafkaListener(topics = TOPIC_NAME)
	public void listenMessage(ChatMessageDto chatMessageDto) {
		try {
			template.convertAndSend("/sub/chat/" + chatMessageDto.getChatRoomId(), chatMessageDto);
			log.info("수신 채팅방: {}", chatMessageDto.getChatRoomId());

		} catch (Exception e) {
			throw new RuntimeException("예외 발생 : " + e.getMessage());
		}
	}

	@KafkaListener(topics = CHAT_UPDATE_TOPIC)
	public void listenForChatUpdates(ChatUpdateDto chatUpdateDto) {
		try {
			List<String> chatUsers = chatRoomUserRepository.findAllByChatRoomIdAndNotMe(chatUpdateDto.getChatRoomId(),
				chatUpdateDto.getSender());
			chatUsers.forEach(nickname -> {
				String destination = "/sub/chat/list/updates/" + nickname;
				template.convertAndSend(destination, chatUpdateDto);
			});
			log.info("Update 수신 채팅방: {}", chatUpdateDto.getChatRoomId());

		} catch (Exception e) {
			throw new RuntimeException("예외 발생 : " + e.getMessage());
		}
	}
}