package com.backend.naildp.common;

import org.springframework.context.annotation.Configuration;

import com.backend.naildp.entity.Profile;
import com.backend.naildp.repository.ProfileRepository;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class ProfileInitializer {
	private final ProfileRepository profileRepository;

	@PostConstruct
	public void init() {

		String baseUrl = "/assets/img/profile/basic/basic_";
		String baseName = "Basic Profile ";
		ProfileType profileType = ProfileType.BASIC;
		boolean profileExist = profileRepository.existsProfileByProfileUrlStartsWith(baseUrl);
		if (!profileExist) {
			for (int i = 1; i <= 19; i++) {
				String profileUrl = baseUrl + i + ".png";
				String name = baseName + i;
				Profile profile = Profile.builder()
					.name(name)
					.thumbnail(false)
					.profileType(profileType)
					.profileUrl(profileUrl)
					.build();
				profileRepository.save(profile);
			}
		}
	}
}
