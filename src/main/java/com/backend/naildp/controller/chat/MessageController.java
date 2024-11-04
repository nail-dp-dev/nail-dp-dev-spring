package com.backend.naildp.controller.chat;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.backend.naildp.dto.chat.ChatMessageDto;
import com.backend.naildp.dto.chat.MessageSummaryResponse;
import com.backend.naildp.exception.ApiResponse;
import com.backend.naildp.oauth2.impl.UserDetailsImpl;
import com.backend.naildp.service.chat.MessageService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = "MessageController")
@RequiredArgsConstructor
@RequestMapping("/api")
@RestController
public class MessageController {
	private final MessageService messageService;

	@MessageMapping("/chat/{chatRoomId}/message")
	public ResponseEntity<ApiResponse<?>> sendMessage(ChatMessageDto chatMessageDto,
		@DestinationVariable("chatRoomId") UUID chatRoomId) {
		UUID currentRoomId = messageService.sendMessage(chatMessageDto, chatRoomId);

		log.info("Message [{}] sent by user: {} to chatting room: {}", chatMessageDto.getContent(),
			chatMessageDto.getSender(), chatRoomId);
		return ResponseEntity.ok(ApiResponse.successResponse(currentRoomId, "메시지 전송 성공", 2000));

	}

	@PostMapping("/chat/{chatRoomId}/images")
	public ResponseEntity<ApiResponse<?>> sendImageMessages(@PathVariable("chatRoomId") UUID chatRoomId,
		@AuthenticationPrincipal UserDetailsImpl userDetails, @RequestPart("images") List<MultipartFile> imageFiles) {

		messageService.sendImageMessages(chatRoomId, userDetails.getUser().getNickname(),
			imageFiles);
		return ResponseEntity.ok(ApiResponse.successResponse(null, "이미지 메시지 전송 성공", 2001));

	}

	@PostMapping("/chat/{chatRoomId}/video")
	public ResponseEntity<ApiResponse<?>> sendVideoMessage(@PathVariable("chatRoomId") UUID chatRoomId,
		@AuthenticationPrincipal UserDetailsImpl userDetails, @RequestPart("video") MultipartFile video) {

		messageService.sendVideoMessage(chatRoomId, userDetails.getUser().getNickname(),
			video);

		return ResponseEntity.ok(ApiResponse.successResponse(null, "동영상 메시지 전송 성공", 2001));

	}

	@PostMapping("/chat/{chatRoomId}/file")
	public ResponseEntity<ApiResponse<?>> sendFileMessages(@PathVariable("chatRoomId") UUID chatRoomId,
		@AuthenticationPrincipal UserDetailsImpl userDetails, @RequestPart("file") MultipartFile file) {

		messageService.sendFileMessage(chatRoomId, userDetails.getUser().getNickname(),
			file);
		return ResponseEntity.ok(ApiResponse.successResponse(null, "파일 메시지 전송 성공", 2001));

	}

	@GetMapping("/chat/{chatRoomId}")
	public ResponseEntity<ApiResponse<?>> getMessagesByRoomId(@PathVariable("chatRoomId") UUID chatRoomId,
		@AuthenticationPrincipal UserDetailsImpl userDetails) {
		MessageSummaryResponse messageResponseDto = messageService.getMessagesByRoomId(chatRoomId,
			userDetails.getUser().getNickname());
		return ResponseEntity.ok(ApiResponse.successResponse(messageResponseDto, "특정 메시지 조회 성공", 2000));
	}

}
