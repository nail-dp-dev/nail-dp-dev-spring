package com.backend.naildp.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.backend.naildp.dto.search.SearchUserResponse;
import com.backend.naildp.exception.ApiResponse;
import com.backend.naildp.service.SearchService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
public class SearchController {

	private final SearchService searchService;

	@GetMapping
	ResponseEntity<?> searchUsers(@RequestParam(name = "keyword", defaultValue = "") String keyword,
		@AuthenticationPrincipal UserDetails userDetails) {
		List<SearchUserResponse> response = searchService.searchUsers(keyword, userDetails.getUsername());
		return ResponseEntity.ok(ApiResponse.successResponse(response, "사용자 검색 성공", 2000));
	}
}
