package com.backend.naildp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.backend.naildp.jwt.ExceptionHandlerFilter;
import com.backend.naildp.jwt.JwtAuthenticationFilter;
import com.backend.naildp.jwt.JwtAuthorizationFilter;
import com.backend.naildp.jwt.JwtUtil;
import com.backend.naildp.security.UserDetailsServiceImpl;
import com.backend.naildp.service.CustomOAuth2UserService;

@Configuration
@EnableWebSecurity // Spring Security 지원을 가능하게 함
@EnableMethodSecurity
// @EnableGlobalMethodSecurity(securedEnabled = true)
public class WebSecurityConfig {

	private final JwtUtil jwtUtil;
	private final UserDetailsServiceImpl userDetailsService;
	//authenticationManager를 가져오기위해ㅔ
	private final AuthenticationConfiguration authenticationConfiguration;

	private final ExceptionHandlerFilter exceptionHandlerFilter;

	private final CustomOAuth2UserService customOAuth2UserService;

	public WebSecurityConfig(JwtUtil jwtUtil, UserDetailsServiceImpl userDetailsService,
		AuthenticationConfiguration authenticationConfiguration, ExceptionHandlerFilter exceptionHandlerFilter,
		CustomOAuth2UserService customOAuth2UserService) {
		this.jwtUtil = jwtUtil;
		this.userDetailsService = userDetailsService;
		this.authenticationConfiguration = authenticationConfiguration;
		this.exceptionHandlerFilter = exceptionHandlerFilter;
		this.customOAuth2UserService = customOAuth2UserService;
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
		return new JwtAuthorizationFilter(jwtUtil, userDetailsService);
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();

		configuration.addAllowedOrigin("http://127.0.0.1:3000");
		configuration.addAllowedOrigin("http://localhost:3000");
		configuration.addAllowedHeader("*");
		configuration.addAllowedMethod("*");
		configuration.setAllowCredentials(true);
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		// CSRF 설정
		http.csrf((csrf) -> csrf.disable());

		http.cors(cors -> cors.configurationSource(corsConfigurationSource()));
		// 기본 설정인 Session 방식은 사용하지 않고 JWT 방식을 사용하기 위한 설정
		http.sessionManagement((sessionManagement) ->
			sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
		);

		http.anonymous(AbstractHttpConfigurer::disable);

		http.authorizeHttpRequests((authorizeHttpRequests) ->
			authorizeHttpRequests
				.requestMatchers("/").permitAll() // '/api/auth/'로 시작하는 요청 모두 접근 허가
				.requestMatchers("/auth/**").permitAll() // '/api/auth/'로 시작하는 요청 모두 접근 허가
				.requestMatchers("/auth/signup").permitAll() // '/api/auth/'로 시작하는 요청 모두 접근 허가
				.requestMatchers("/home").permitAll() // '/api/auth/'로 시작하는 요청 모두 접근 허가
				.anyRequest().authenticated() // 그 외 모든 요청 인증처리

		);

		http.oauth2Login(oauth2Configurer -> oauth2Configurer
			.userInfoEndpoint(userInfoEndpointConfig -> userInfoEndpointConfig
				.userService(customOAuth2UserService))); // 해당 서비스 로직을 타도록 설정)

		// 필터 관리
		http.addFilterBefore(jwtAuthorizationFilter(), UsernamePasswordAuthenticationFilter.class);
		http.addFilterBefore(exceptionHandlerFilter, JwtAuthorizationFilter.class);
		// http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}
}