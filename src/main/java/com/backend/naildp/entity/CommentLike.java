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
//@Entity
//@Getter
//@DiscriminatorValue("comment")
//@NoArgsConstructor(access = AccessLevel.PROTECTED)
//public class CommentLike extends Like {
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "user_id")
//    private User user;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "post_id")
//    private Comment comment;
//
//    public CommentLike(User user, Comment comment) {
//        this.user = user;
//        this.comment = comment;
//    }
//}
