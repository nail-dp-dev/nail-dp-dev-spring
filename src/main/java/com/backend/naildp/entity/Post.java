package com.backend.naildp.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private String postContent;

    @Column(nullable = false)
    private Long sharing;

    @Column(nullable = false)
    private Boolean closer; // FOLLOW, ALL, NONE

    @Column(nullable = false)
    private Boolean tempSave;

    public Post(User user, String postContent, Long sharing, Boolean closer, Boolean tempSave) {
        this.user = user;
        this.postContent = postContent;
        this.sharing = sharing;
        this.closer = closer;
        this.tempSave = tempSave;
    }
}
