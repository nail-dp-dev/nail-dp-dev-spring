package com.backend.naildp.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.naildp.dto.search.SearchUserResponse;
import com.backend.naildp.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SearchService {

	private final UserRepository userRepository;

	public List<SearchUserResponse> searchUsers(String keyword, String username) {
		return userRepository.searchByKeyword(keyword, username);
	}
}
