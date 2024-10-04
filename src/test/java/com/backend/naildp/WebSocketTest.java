package com.backend.naildp;

import static org.assertj.core.api.Assertions.*;

import java.util.Collections;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.StringMessageConverter;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import com.backend.naildp.common.UserRole;
import com.backend.naildp.oauth2.jwt.JwtUtil;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class WebSocketTest {

	@LocalServerPort
	private int port;

	@Autowired
	private JwtUtil jwtUtil; // 만약 JWT 토큰이 필요하다면 사용

	@Test
	public void testWebSocketConnectionAndMessageSend() throws
		InterruptedException,
		ExecutionException,
		TimeoutException {
		// WebSocket STOMP 클라이언트 설정
		WebSocketStompClient stompClient = new WebSocketStompClient(new SockJsClient(
			Collections.singletonList(new WebSocketTransport(new StandardWebSocketClient()))));
		stompClient.setMessageConverter(new StringMessageConverter());

		// WebSocket 연결 URL
		String url = "ws://localhost:" + port + "/chat";

		// JWT 토큰 생성 (쿠키 인증이 필요한 경우)
		String jwtToken = jwtUtil.createToken("다다음민지", UserRole.USER);  // 가상의 JWT 토큰 생성

		// STOMP 헤더 설정
		StompHeaders headers = new StompHeaders();
		headers.add("Authorization", jwtToken); // 인증이 필요한 경우 헤더에 JWT 추가

		// STOMP 세션 연결
		StompSession stompSession = stompClient.connect(url, new StompSessionHandlerAdapter() {
		}).get(5, TimeUnit.SECONDS);

		// 메시지 전송 테스트
		// String destination = "/pub/123/message"; // 채팅방 ID 123에 메시지 전송
		// stompSession.send(destination, "Test message from backend");

		assertThat(stompSession.isConnected()).isTrue();  // 연결이 성공적으로 되었는지 확인
	}
}
