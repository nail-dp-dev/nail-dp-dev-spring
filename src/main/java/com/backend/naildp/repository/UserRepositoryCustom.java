package com.backend.naildp.repository;

import java.util.List;

import com.backend.naildp.dto.search.SearchUserResponse;
import com.backend.naildp.entity.User;

public interface UserRepositoryCustom {

	List<SearchUserResponse> searchByKeyword(String keyword, String nickname);

	List<SearchUserResponse> findRecommendedUser(User currentUser);
}
