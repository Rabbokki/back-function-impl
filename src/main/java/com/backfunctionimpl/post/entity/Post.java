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

    // 🔹 리뷰 수 (추가된 필드)
    private int reviewSize;

    // 🔹 평균 평점 (추가된 필드)
    private int totalRating;
    private float averageRating;

    // 🔹 리뷰 수 업데이트 메서드 (추가된 메서드)
    public void setReviewSize(int reviewSize) {
        this.reviewSize = reviewSize;
    }

    // 🔹 평균 평점 재계산 메서드 (추가된 메서드)
    public void recalculateAverageRating() {
        // 여기서 실제로 평균을 계산하는 로직을 작성해야 합니다.
        // 예시로는 리뷰들의 평균 평점을 계산하는 방식입니다.
        if (this.reviewSize > 0) {
            // 실제로는 리뷰들의 평점을 기준으로 평균을 계산해야 합니다.
            // 예를 들어 리뷰들의 평점 합을 가져와 평균을 계산해야 합니다.
            // 임시로 0으로 설정해두었습니다.
            this.averageRating = totalRating / reviewSize;  // 여기에서 실제 평점 평균을 계산해야 합니다.
        } else {
            this.averageRating = 0;  // 리뷰가 없다면 평균 평점은 0
        }
    }

    public void commentUpdate(int size) {
        this.commentsCount = size;
    }

    public void likeUpdate(int size) {
        this.likeCount = size;
    }
}