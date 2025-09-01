package com.backend.naildp.service.post.dto;

import java.util.Objects;

import org.springframework.security.core.Authentication;

import com.backend.naildp.service.post.v3.AuthenticationStatus;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserContext {

	private String username;
	private AuthenticationStatus authenticationStatus;

	private UserContext(String username, AuthenticationStatus authenticationStatus) {
		this.username = username;
		this.authenticationStatus = authenticationStatus;
	}

	public static UserContext ofAuthenticatedUser(Authentication authentication) {
		return new UserContext(authentication.getName(), AuthenticationStatus.AUTHENTICATED);
	}

	public static UserContext ofUnauthenticatedUser() {
		return new UserContext("", AuthenticationStatus.UNAUTHENTICATED);
	}

	public static UserContext from(Authentication authentication) {
		return Objects.isNull(authentication) ? ofUnauthenticatedUser() : ofAuthenticatedUser(authentication);
	}

	public boolean isUnauthenticated() {
		return authenticationStatus.isUnauthenticated();
	}

	public boolean isAuthenticated() {
		return authenticationStatus.isAuthenticated();
	}
}
