package com.backend.naildp.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.backend.naildp.dto.chat.ChatMessageDto;
import com.backend.naildp.entity.mongo.ChatMessage;
import com.backend.naildp.service.ChatMessageService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatMessageController {

	private final ChatMessageService chatMessageService;

	@PostMapping("/message")
	public ResponseEntity<ChatMessage> createChatMessage(@RequestBody ChatMessageDto chatMessageDto) {
		ChatMessage savedMessage = chatMessageService.createChatMessage(chatMessageDto);
		return ResponseEntity.ok(savedMessage);
	}
}
