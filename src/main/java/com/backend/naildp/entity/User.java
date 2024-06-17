package com.backend.naildp.entity;

import com.backend.naildp.common.UserRole;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity(name = "Users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "user_id")
    private UUID id;

    @ElementCollection
    @CollectionTable(name = "SOCIAL_LOGIN", joinColumns = @JoinColumn(name = "user_id"))
    private List<SocialLogin> socialLoginList = new ArrayList<>();

    @Column(nullable = false)
    private String nickname;

    @Column(nullable = false)
    private String phoneNumber;
    private String profileUrl;
    private Long point;
    private UserRole role;

    public User(SocialLogin socialLogin, String nickname, String phoneNumber, String profileUrl,
                Long point, UserRole role) {
        this.socialLoginList.add(socialLogin);
        this.nickname = nickname;
        this.phoneNumber = phoneNumber;
        this.profileUrl = profileUrl;
        this.point = point;
        this.role = role;
    }
}
