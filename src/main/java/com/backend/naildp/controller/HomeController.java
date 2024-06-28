package com.backend.naildp.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.backend.naildp.service.PostService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/home")
@RequiredArgsConstructor
public class HomeController {

	private final PostService postService;

	@GetMapping
	public ResponseEntity<?> homePosts(@RequestParam(name = "choice") String choice, @AuthenticationPrincipal
		UserDetails userDetails) {
		return ResponseEntity.ok(postService.homePosts(userDetails.getUsername()));
	}
}
