package com.backend.naildp.service.post.v3;

public enum AuthenticationStatus {

	AUTHENTICATED,
	UNAUTHENTICATED;

	public boolean isUnauthenticated() {
		return this == UNAUTHENTICATED;
	}

	public boolean isAuthenticated() {
		return this == AUTHENTICATED;
	}
}
