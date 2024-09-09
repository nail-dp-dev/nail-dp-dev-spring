package com.backend.naildp.oauth2.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import com.backend.naildp.common.UserRole;
import com.backend.naildp.entity.User;

public class UserDetailsImpl implements UserDetails, OAuth2User {

	private final User user;
	private Map<String, Object> attributes;

	public UserDetailsImpl(User user) {
		this.user = user;
	}

	//OAuth 로그인
	public UserDetailsImpl(User user, Map<String, Object> attributes) {
		this.user = user;
		this.attributes = attributes;
	}

	//OAuth2User의 메서드
	@Override
	public Map<String, Object> getAttributes() {
		return attributes;
	}

	public User getUser() {
		return user;
	}

	@Override
	public String getPassword() {
		return null;
	}

	@Override
	public String getUsername() {
		return user.getNickname();
	}

	//인증객체 생성
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		UserRole role = user.getRole();
		String authority = role.getAuthority();

		SimpleGrantedAuthority simpleGrantedAuthority = new SimpleGrantedAuthority(authority);
		Collection<GrantedAuthority> authorities = new ArrayList<>();
		authorities.add(simpleGrantedAuthority);

		return authorities;
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public String getName() {
		return null;
	}
}
