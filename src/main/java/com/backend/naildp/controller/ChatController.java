package com.backend.naildp.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.backend.naildp.dto.chat.ChatListSummaryResponse;
import com.backend.naildp.dto.chat.ChatMessageDto;
import com.backend.naildp.dto.chat.ChatRoomRequestDto;
import com.backend.naildp.dto.chat.MessageSummaryResponse;
import com.backend.naildp.exception.ApiResponse;
import com.backend.naildp.oauth2.impl.UserDetailsImpl;
import com.backend.naildp.service.ChatService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = "Chat")
@RequiredArgsConstructor
@RequestMapping("/api")
@RestController
public class ChatController {
	private final ChatService chatService;
	private final SimpMessagingTemplate simpMessagingTemplate;

	@PostMapping("/chat")
	public ResponseEntity<ApiResponse<?>> createChatRoom(@AuthenticationPrincipal UserDetailsImpl userDetails,
		@RequestBody ChatRoomRequestDto chatRoomRequestDto) {
		UUID chatRoomId = chatService.createChatRoom(userDetails.getUser().getNickname(), chatRoomRequestDto);
		return ResponseEntity.ok(ApiResponse.successResponse(chatRoomId, "채팅방 생성 성공", 2001));
	}

	@MessageMapping("chat/{chatRoomId}/message")

	public void sendMessage(ChatMessageDto chatMessageDto,
		@DestinationVariable("chatRoomId") UUID chatRoomId) {
		String chatId = chatService.sendMessage(chatMessageDto, chatRoomId);
		simpMessagingTemplate.convertAndSend("/sub/chat/" + chatRoomId, chatMessageDto);

		log.info("Message [{}] sent by user: {} to chatting room: {}",
			chatMessageDto.getContent(),
			chatMessageDto.getSender(),
			chatRoomId);

	}

	@GetMapping("chat/list")
	public ResponseEntity<ApiResponse<?>> getChatList(@AuthenticationPrincipal UserDetailsImpl userDetails) {
		ChatListSummaryResponse response = chatService.getChatList(userDetails.getUser().getNickname());
		return ResponseEntity.ok(ApiResponse.successResponse(response, "채팅방 목록 조회 성공", 2000));
	}

	@GetMapping("chat/{chatRoomId}")
	public ResponseEntity<ApiResponse<?>> getMessagesByRoomId(@PathVariable("chatRoomId") UUID chatRoomId) {
		MessageSummaryResponse messageResponseDto = chatService.getMessagesByRoomId(chatRoomId);
		return ResponseEntity.ok(ApiResponse.successResponse(messageResponseDto, "특정 메시지 조회 성공", 2000));
	}

}
