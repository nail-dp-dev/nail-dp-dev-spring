package com.backend.naildp.repository;

import java.util.List;

import com.backend.naildp.dto.search.SearchUserResponse;

public interface UserRepositoryCustom {

	List<SearchUserResponse> searchByKeyword(String keyword, String nickname);
}
