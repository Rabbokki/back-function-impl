package com.backfunctionimpl.post.entity;

import com.backfunctionimpl.account.entity.Account;
import com.backfunctionimpl.account.entity.BaseEntity;
import com.backfunctionimpl.post.enums.Category;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "post")
public class Post extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    private String imgUrl; // 리스트 → 단일 문자열


    @Enumerated(EnumType.STRING)
    private Category category;

    private int views;

    private int commentsCount;

    private int likeCount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private Account account;
}
