package com.backend.naildp.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.backend.naildp.common.UserRole;
import com.backend.naildp.entity.User;
import com.backend.naildp.repository.UserRepository;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

	private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
	private final UserRepository userRepository;

	@GetMapping("/login")
	String adminLoginPage() {
		log.info("로그인 페이지 접속");
		return "ok1";
	}

	@ResponseStatus(value = HttpStatus.OK)
	@PostMapping("/login")
	String adminLogin(@RequestBody AdminLoginRequest adminLoginRequest) {
		log.info("회원가입 시도");
		String encodedPassword = passwordEncoder.encode(adminLoginRequest.getPassword());
		User admin = User.builder().nickname("관리자").phoneNumber("").agreement(true).role(UserRole.ADMIN).build();
		admin.setLoginId(adminLoginRequest.getLoginId());
		admin.addAdminPassword(encodedPassword);
		userRepository.save(admin);
		return "ok2";
	}

	@Getter
	@AllArgsConstructor
	@NoArgsConstructor
	static class AdminLoginRequest {
		private String loginId;
		private String password;
	}

	@Getter
	@AllArgsConstructor
	@NoArgsConstructor
	static class AdminLoginResponse {
		private String msg;
	}
}
