package com.backfunctionimpl.post.entity;

import com.backfunctionimpl.account.entity.Account;
import com.backfunctionimpl.account.entity.BaseEntity;
import com.backfunctionimpl.comment.entity.Comment;
import com.backfunctionimpl.post.entity.Image; // ← 이 부분 꼭 확인
import com.backfunctionimpl.post.enums.Category;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

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

    @Enumerated(EnumType.STRING)
    private Category category;

    private int views;
    private int commentsCount;
    private int likeCount;

    // 🔹 이미지 필드 추가
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Image> images = new ArrayList<>();

    // 🔹 댓글
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> commentList = new ArrayList<>();

    // 🔹 태그
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostTag> tags = new ArrayList<>();

    // 🔹 작성자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private Account account;

    public void commentUpdate(int size) {
        this.commentsCount=size;
    }

    public void likeUpdate(int size) {
        this.likeCount=size;
    }
}
