//package com.backfunctionimpl.review.entity;
//
//
//import com.backendfunction.account.entity.Account;
//import com.backendfunction.account.entity.BaseEntity;
//import com.backendfunction.post.entity.Post;
//import jakarta.persistence.*;
//import lombok.Getter;
//import lombok.NoArgsConstructor;
//import lombok.Setter;
//
//@Entity
//@Getter
//@Setter
//@NoArgsConstructor
//public class Review extends BaseEntity {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(nullable = false)
//    private Post post;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(nullable = false)
//    private Account account;
//
//    @Column(nullable = false)
//    private int rating;
//
//    @Column(length = 500)
//    private String nickname;
//
//    @Column(length = 500)
//    private String content;
//
//    public Review(Post post, Account account, int rating, String nickname, String content) {
//        this.post = post;
//        this.account = account;
//        this.rating = rating;
//        this.nickname = nickname;
//        this.content = content;
//    }
//}