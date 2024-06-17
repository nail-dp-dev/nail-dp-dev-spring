//package com.backend.naildp.entity;
//
//import jakarta.persistence.DiscriminatorValue;
//import jakarta.persistence.Entity;
//import jakarta.persistence.FetchType;
//import jakarta.persistence.JoinColumn;
//import jakarta.persistence.ManyToOne;
//import lombok.AccessLevel;
//import lombok.Getter;
//import lombok.NoArgsConstructor;
//
////@Entity
//@Getter
//@DiscriminatorValue("post")
//@NoArgsConstructor(access = AccessLevel.PROTECTED)
//public class PostLike extends Like {
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "user_id")
//    private User user;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "post_id")
//    private Post post;
//
//    public PostLike(User user, Post post) {
//        this.user = user;
//        this.post = post;
//    }
//}
