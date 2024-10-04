package com.backend.naildp.service;

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
			.content(chatMessageDto.getContent())
			.sender(chatMessageDto.getSender())
			.mention(chatMessageDto.getMention())
			.build();

		return chatMessageRepository.save(chatMessage);
	}
}

