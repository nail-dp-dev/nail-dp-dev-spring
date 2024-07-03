// package com.backend.naildp.service;
//
// import static org.springframework.test.util.AssertionErrors.*;
//
// import org.junit.jupiter.api.DisplayName;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.Mock;
// import org.mockito.junit.jupiter.MockitoExtension;
//
// import com.backend.naildp.common.CookieUtil;
// import com.backend.naildp.jwt.JwtUtil;
// import com.backend.naildp.repository.ProfileRepository;
// import com.backend.naildp.repository.SocialLoginRepository;
// import com.backend.naildp.repository.UserRepository;
//
// @ExtendWith(MockitoExtension.class) //@Mock 사용
// class AuthServiceTest {
//
// 	@Mock
// 	UserRepository userRepository;
// 	@Mock
// 	CookieUtil cookieUtil;
// 	@Mock
// 	SocialLoginRepository socialLoginRepository;
// 	@Mock
// 	ProfileRepository profileRepository;
// 	@Mock
// 	JwtUtil jwtUtil;
//
// 	@Test
// 	void signupUser() {
// 	}
//
// 	@Test
// 	@DisplayName("중복된 닉네임이 존재")
// 	void duplicateNickname() {
// 		//given
// 		String nickname = "alwl123";
// 		AuthService authService = new AuthService(userRepository, cookieUtil, socialLoginRepository, profileRepository,
// 			jwtUtil);
// 		//when
// 		authService.duplicateNickname(nickname);
// 		//then
// 		assertEquals()
// 	}
//
// 	@Test
// 	void duplicatePhone() {
// 	}
// }