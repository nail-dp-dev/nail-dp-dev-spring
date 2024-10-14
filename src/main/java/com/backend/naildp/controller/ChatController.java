package com.backend.naildp.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.backend.naildp.dto.chat.ChatListSummaryResponse;
import com.backend.naildp.dto.chat.ChatMessageDto;
import com.backend.naildp.dto.chat.ChatRoomRequestDto;
import com.backend.naildp.dto.chat.ChatUpdateDto;
import com.backend.naildp.dto.chat.MessageSummaryResponse;
import com.backend.naildp.exception.ApiResponse;
import com.backend.naildp.oauth2.impl.UserDetailsImpl;
import com.backend.naildp.service.ChatRoomStatusService;
import com.backend.naildp.service.ChatService;
import com.backend.naildp.service.KafkaProducerService;
import com.backend.naildp.service.MessageStatusService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = "Chat")
@RequiredArgsConstructor
@RequestMapping("/api")
@RestController
public class ChatController {
	private final ChatService chatService;
	private final SimpMessagingTemplate simpMessagingTemplate;
	private final KafkaProducerService kafkaProducerService;
	private final ChatRoomStatusService chatRoomStatusService;
	private final MessageStatusService messageStatusService;

	@PostMapping("/chat")
	public ResponseEntity<ApiResponse<?>> createChatRoom(@AuthenticationPrincipal UserDetailsImpl userDetails,
		@RequestBody ChatRoomRequestDto chatRoomRequestDto) {
		UUID chatRoomId = chatService.createChatRoom(userDetails.getUser().getNickname(), chatRoomRequestDto);
		return ResponseEntity.ok(ApiResponse.successResponse(chatRoomId, "채팅방 생성 성공", 2001));
	}

	@MessageMapping("chat/{chatRoomId}/message")
	public void sendMessage(ChatMessageDto chatMessageDto,
		@DestinationVariable("chatRoomId") UUID chatRoomId) {
		String messageId = chatService.sendMessage(chatMessageDto, chatRoomId);

		List<String> nicknames = chatService.getChatRoomNickname(chatRoomId); // 방 참여자 목록
		nicknames.forEach(nickname -> {
			if (!nickname.equals(chatMessageDto.getSender())) {
				chatRoomStatusService.incrementUnreadCount(chatRoomId.toString(), nickname);
				messageStatusService.setFirstUnreadMessageId(chatRoomId.toString(), nickname, messageId);
			}
		});

		int unreadCount = chatRoomStatusService.getUnreadCount(chatRoomId.toString(), chatMessageDto.getSender());
		ChatUpdateDto chatUpdateDto = new ChatUpdateDto(
			chatRoomId.toString(),
			unreadCount,
			chatMessageDto.getContent().get(0),
			LocalDateTime.now()
		);
		kafkaProducerService.send(chatMessageDto);

		kafkaProducerService.sendChatUpdate(chatUpdateDto);

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
	public ResponseEntity<ApiResponse<?>> getMessagesByRoomId(@PathVariable("chatRoomId") UUID chatRoomId,
		@AuthenticationPrincipal UserDetailsImpl userDetails) {
		MessageSummaryResponse messageResponseDto = chatService.getMessagesByRoomId(chatRoomId,
			userDetails.getUser().getNickname());
		return ResponseEntity.ok(ApiResponse.successResponse(messageResponseDto, "특정 메시지 조회 성공", 2000));
	}

	@PostMapping("chat/{chatRoomId}/images")
	public ResponseEntity<ApiResponse<?>> sendImageMessages(
		@PathVariable("chatRoomId") UUID chatRoomId,
		@AuthenticationPrincipal UserDetailsImpl userDetails,
		@RequestParam("images") List<MultipartFile> imageFiles) {

		ChatMessageDto chatMessageDto = chatService.sendImageMessages(chatRoomId, userDetails.getUser().getNickname(),
			imageFiles);
		kafkaProducerService.send(chatMessageDto);
		return ResponseEntity.ok(ApiResponse.successResponse(chatMessageDto, "이미지 메시지 전송 성공", 2001));

	}

	@DeleteMapping("chat/{chatRoomId}/leave")
	public ResponseEntity<ApiResponse<?>> leaveChatRoom(
		@PathVariable("chatRoomId") UUID chatRoomId,
		@AuthenticationPrincipal UserDetailsImpl userDetails) {

		chatService.leaveChatRoom(chatRoomId, userDetails.getUsername());
		return ResponseEntity.ok(ApiResponse.successResponse(null, "채팅방에서 나갔습니다.", 2001));
	}

}
