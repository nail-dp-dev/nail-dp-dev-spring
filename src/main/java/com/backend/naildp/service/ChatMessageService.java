package com.backend.naildp.service;

import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.backend.naildp.dto.chat.ChatMessageDto;
import com.backend.naildp.entity.mongo.ChatMessage;
import com.backend.naildp.repository.mongo.ChatMessageRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatMessageService {

	private final ChatMessageRepository chatMessageRepository;

	public ChatMessage createChatMessage(ChatMessageDto chatMessageDto) {
		ChatMessage chatMessage = ChatMessage.builder()
			.content(
				chatMessageDto.getContent().stream()
					.map(contentDTO -> ChatMessage.Content.builder()
						.type(contentDTO.getType())
						.value(contentDTO.getValue())
						.build()
					).collect(Collectors.toList())
			)
			.status(chatMessageDto.getStatus())
			.userId(chatMessageDto.getUserId())
			.chatRoomId(chatMessageDto.getChatRoomId())
			.mention(chatMessageDto.getMention())
			.build();

		return chatMessageRepository.save(chatMessage);
	}
}

