package com.backend.naildp.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.backend.naildp.common.CookieUtil;
import com.backend.naildp.oauth2.CustomAuthorizationRequestRepository;
import com.backend.naildp.oauth2.CustomOAuth2UserService;
import com.backend.naildp.oauth2.handler.CustomAccessDeniedHandler;
import com.backend.naildp.oauth2.handler.CustomAuthenticationEntryPoint;
import com.backend.naildp.oauth2.handler.OAuth2AuthenticationFailureHandler;
import com.backend.naildp.oauth2.handler.OAuth2AuthenticationSuccessHandler;
import com.backend.naildp.oauth2.impl.UserDetailsServiceImpl;
import com.backend.naildp.oauth2.jwt.ExceptionHandlerFilter;
import com.backend.naildp.oauth2.jwt.JwtAuthenticationFilter;
import com.backend.naildp.oauth2.jwt.JwtAuthorizationFilter;
import com.backend.naildp.oauth2.jwt.JwtUtil;
import com.backend.naildp.oauth2.jwt.RedisUtil;
import com.backend.naildp.repository.UserRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableWebSecurity // Spring Security 지원을 가능하게 함
@EnableMethodSecurity
// @EnableGlobalMethodSecurity(securedEnabled = true)
public class WebSecurityConfig {

	private final JwtUtil jwtUtil;
	private final UserDetailsServiceImpl userDetailsService;
	private final RedisUtil redisUtil;
	private final UserRepository userRepository;
	private final AuthenticationConfiguration authenticationConfiguration;

	private final ExceptionHandlerFilter exceptionHandlerFilter;

	private final CustomOAuth2UserService customOAuth2UserService;
	private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
	private final OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;

	private final CookieUtil cookieutil;

	@Value("${spring.server.domain}")
	private String domain;

	public WebSecurityConfig(JwtUtil jwtUtil, UserDetailsServiceImpl userDetailsService, RedisUtil redisUtil,
		UserRepository userRepository, AuthenticationConfiguration authenticationConfiguration,
		ExceptionHandlerFilter exceptionHandlerFilter, CustomOAuth2UserService customOAuth2UserService,
		OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler,
		OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler, CookieUtil cookieutil) {
		this.jwtUtil = jwtUtil;
		this.userDetailsService = userDetailsService;
		this.redisUtil = redisUtil;
		this.userRepository = userRepository;
		this.authenticationConfiguration = authenticationConfiguration;
		this.exceptionHandlerFilter = exceptionHandlerFilter;
		this.customOAuth2UserService = customOAuth2UserService;
		this.oAuth2AuthenticationSuccessHandler = oAuth2AuthenticationSuccessHandler;
		this.oAuth2AuthenticationFailureHandler = oAuth2AuthenticationFailureHandler;
		this.cookieutil = cookieutil;
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
		return configuration.getAuthenticationManager();
	}

	@Bean
	public JwtAuthenticationFilter jwtAuthenticationFilter() throws Exception {
		JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtUtil);
		filter.setAuthenticationManager(authenticationManager(authenticationConfiguration));
		return filter;
	}

	@Bean
	public JwtAuthorizationFilter jwtAuthorizationFilter() {
		return new JwtAuthorizationFilter(jwtUtil, userDetailsService, redisUtil, userRepository);
	}

	@Bean
	public WebSecurityCustomizer webSecurityCustomizer() {
		return web -> web.ignoring()
			.requestMatchers("/error", "/favicon.ico");
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();

		configuration.addAllowedOrigin("http://localhost:3000");
		configuration.addAllowedOrigin("https://localhost:3000");
		configuration.addAllowedOrigin(domain + ":3000");
		configuration.addAllowedOrigin(domain);

		configuration.addAllowedHeader("*");
		configuration.addAllowedMethod("*");
		configuration.setAllowCredentials(true);
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}

	@Bean
	public CustomAuthorizationRequestRepository customAuthorizationRequestRepository() {
		return new CustomAuthorizationRequestRepository(cookieutil);
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		// CSRF 설정
		http.csrf((csrf) -> csrf.disable());

		http.cors(cors -> cors.configurationSource(corsConfigurationSource()));
		// 기본 설정인 Session 방식은 사용하지 않고 JWT 방식을 사용하기 위한 설정
		http.sessionManagement(
			(sessionManagement) -> sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

		http.anonymous(AbstractHttpConfigurer::disable);

		http.authorizeHttpRequests((authorizeHttpRequests) -> authorizeHttpRequests.requestMatchers("/")
			.permitAll()
			.requestMatchers("/ws-stomp/**").permitAll()  // WebSocket 엔드포인트 예외 처리
			.requestMatchers("/api/auth/**")
			.permitAll() // '/api/auth/'로 시작하는 요청 모두 접근 허가
			.requestMatchers("/api/portone/**").permitAll()
			.requestMatchers("/api/portone").permitAll()
			.requestMatchers("/certificate.html").permitAll()
			.requestMatchers("/api/home").permitAll()
			.requestMatchers("/api/notifications/subscribe").permitAll()
			.requestMatchers("/actuator/**").permitAll()
			.anyRequest()
			.authenticated() // 그 외 모든 요청 인증처리

		);

		http.oauth2Login(oauth2Configurer -> oauth2Configurer
			.authorizationEndpoint(authorizationEndpointConfig -> {
				log.info("Configuring OAuth2 authorization endpoint...");
				authorizationEndpointConfig.baseUri("/api/oauth2/authorization");
				authorizationEndpointConfig.authorizationRequestRepository(customAuthorizationRequestRepository());

			})
			.redirectionEndpoint(redirectionEndpointConfig -> {
				log.info("Configuring OAuth2 redirection endpoint...");
				redirectionEndpointConfig.baseUri("/api/login/oauth2/code/*");
			})
			.userInfoEndpoint(userInfoEndpointConfig -> {
				log.info("Configuring OAuth2 user info endpoint...");
				userInfoEndpointConfig.userService(customOAuth2UserService);
			})
			.successHandler(oAuth2AuthenticationSuccessHandler)
			.failureHandler(oAuth2AuthenticationFailureHandler)
		);

		// 필터 관리
		http.addFilterBefore(jwtAuthorizationFilter(), UsernamePasswordAuthenticationFilter.class);
		http.addFilterBefore(exceptionHandlerFilter, JwtAuthorizationFilter.class);
		// http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

		http.exceptionHandling((exceptions) -> exceptions.authenticationEntryPoint(new CustomAuthenticationEntryPoint())
			.accessDeniedHandler(new CustomAccessDeniedHandler()));
		return http.build();
	}
}