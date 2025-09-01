package com.backend.naildp.service.post.v3;

import java.util.Objects;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.backend.naildp.service.post.dto.UserContext;

@Component
public class UserContextProvider {

	public UserContext getUsernameFromAuthentication() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		return UserContext.from(authentication);
	}
}
