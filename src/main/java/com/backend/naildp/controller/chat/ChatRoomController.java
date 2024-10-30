package com.backend.naildp.controller.chat;

import java.util.List;
import java.util.UUID;

import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.backend.naildp.dto.chat.ChatListSummaryResponse;
import com.backend.naildp.dto.chat.ChatRoomRequestDto;
import com.backend.naildp.dto.chat.RenameChatRoomRequestDto;
import com.backend.naildp.dto.search.SearchUserResponse;
import com.backend.naildp.exception.ApiResponse;
import com.backend.naildp.oauth2.impl.UserDetailsImpl;
import com.backend.naildp.service.S3Service;
import com.backend.naildp.service.chat.ChatRoomService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = "ChatRoomController")
@RequiredArgsConstructor
@RequestMapping("/api")
@RestController
public class ChatRoomController {
	private final ChatRoomService chatRoomService;
	private final S3Service s3Service;

	@PostMapping("/chat")
	public ResponseEntity<ApiResponse<?>> createChatRoom(@AuthenticationPrincipal UserDetailsImpl userDetails,
		@RequestBody ChatRoomRequestDto chatRoomRequestDto) {
		UUID chatRoomId = chatRoomService.createChatRoom(userDetails.getUser().getNickname(), chatRoomRequestDto);
		return ResponseEntity.ok(ApiResponse.successResponse(chatRoomId, "채팅방 생성 성공", 2001));
	}

	@GetMapping("/chat/list")
	public ResponseEntity<ApiResponse<?>> getChatList(@AuthenticationPrincipal UserDetailsImpl userDetails,
		@RequestParam(required = false, defaultValue = "all", value = "category") String category,
		@RequestParam(required = false, defaultValue = "20", value = "size") int size,
		@RequestParam(required = false, value = "cursorId") UUID cursorId) {
		ChatListSummaryResponse response = chatRoomService.getChatList(userDetails.getUser().getNickname(), category,
			size,
			cursorId);
		return ResponseEntity.ok(ApiResponse.successResponse(response, "채팅방 목록 조회 성공", 2000));
	}

	@PatchMapping("/chat/{chatRoomId}/leave")
	public ResponseEntity<ApiResponse<?>> leaveChatRoom(@PathVariable("chatRoomId") UUID chatRoomId,
		@AuthenticationPrincipal UserDetailsImpl userDetails) {

		chatRoomService.leaveChatRoom(chatRoomId, userDetails.getUser().getNickname());
		return ResponseEntity.ok(ApiResponse.successResponse(null, "채팅방에서 나갔습니다", 2001));
	}

	@PatchMapping("/chat/{chatRoomId}/pinning")
	public ResponseEntity<ApiResponse<?>> pinByChatRoomUser(@PathVariable("chatRoomId") UUID chatRoomId,
		@AuthenticationPrincipal UserDetailsImpl userDetails) {

		chatRoomService.pinByChatRoomUser(chatRoomId, userDetails.getUser().getNickname());
		return ResponseEntity.ok(ApiResponse.successResponse(null, "해당 채팅방을 고정했습니다", 2001));

	}

	@PatchMapping("/chat/{chatRoomId}/unpinning")
	public ResponseEntity<ApiResponse<?>> unpinByChatRoomUser(@PathVariable("chatRoomId") UUID chatRoomId,
		@AuthenticationPrincipal UserDetailsImpl userDetails) {

		chatRoomService.unpinByChatRoomUser(chatRoomId, userDetails.getUser().getNickname());
		return ResponseEntity.ok(ApiResponse.successResponse(null, "해당 채팅방을 고정 해제했습니다", 2001));

	}

	@PatchMapping("/chat/{chatRoomId}")
	public ResponseEntity<ApiResponse<?>> renameChatRoom(@PathVariable("chatRoomId") UUID chatRoomId,
		@AuthenticationPrincipal UserDetailsImpl userDetails, @RequestBody RenameChatRoomRequestDto request) {
		chatRoomService.renameChatRoom(chatRoomId, userDetails.getUser().getNickname(), request);
		return ResponseEntity.ok(ApiResponse.successResponse(null, "해당 채팅방을 이름을 변경했습니다", 2001));
	}

	@GetMapping("chat/file")
	public ResponseEntity<InputStreamResource> downloadFile(@RequestParam("fileName") String fileName) {
		InputStreamResource resource = s3Service.downloadFile(fileName);
		String originalFileName = s3Service.extractOriginalFileName(fileName);
		String encodedFileName = s3Service.encodeFileName(originalFileName);

		return ResponseEntity.ok()
			.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + encodedFileName + "\"")
			.contentType(MediaType.APPLICATION_OCTET_STREAM)
			.body(resource);
	}

	@GetMapping("chat/recommend")
	public ResponseEntity<ApiResponse<?>> getRecommendUsers(@AuthenticationPrincipal UserDetailsImpl userDetails) {
		List<SearchUserResponse> response = chatRoomService.getRecommendUsers(userDetails.getUser().getNickname());
		return ResponseEntity.ok(ApiResponse.successResponse(response, "추천 사용자 조회 성공", 2000));

	}

	@GetMapping("chat/recent")
	public ResponseEntity<ApiResponse<?>> getRecentUsers(@AuthenticationPrincipal UserDetailsImpl userDetails) {
		List<SearchUserResponse> response = chatRoomService.getRecentUsers(userDetails.getUser().getNickname());
		return ResponseEntity.ok(ApiResponse.successResponse(response, "최  사용자 조회 성공", 2000));
	}

	@GetMapping("chat/search")
	public ResponseEntity<ApiResponse<?>> searchChatRoomsByName(@AuthenticationPrincipal UserDetailsImpl userDetails,
		@RequestParam("keyword") String keyword,
		@PageableDefault(size = 20) Pageable pageable,
		@RequestParam(name = "cursorId", required = false) UUID cursorId) {
		ChatListSummaryResponse response = chatRoomService.searchChatRoomsByName(userDetails.getUser().getNickname(),
			keyword, pageable, cursorId);
		return ResponseEntity.ok(ApiResponse.successResponse(response, "채팅방 이름 검색 성공", 2000));
	}

}
