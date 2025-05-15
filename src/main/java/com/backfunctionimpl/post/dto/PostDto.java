package com.backfunctionimpl.post.dto;

import com.backfunctionimpl.account.entity.Account;
import com.backfunctionimpl.post.enums.Category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@Builder
public class PostDto {
    private Long id;
    private Long userId;
    private String userName;
    private String userImgUrl;
    private String title;
    private String content;
    private List<String> imgUrl;
    private Category category;
    private int views;
    private int commentsCount;
    private int likeCount;
    private int reviewSize;
    private int totalRating;
    private float averageRating;
    private List<String> tags;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 모든 필드를 받는 생성자 추가
    public PostDto(Long id, Long userId, String userName, String userImgUrl, String title, String content, List<String> imgUrl, Category category,
                   int views, int commentsCount, int likeCount, int reviewSize, int totalRating, float averageRating, List<String> tags, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.userId = userId;
        this.userName = userName;
        this.userImgUrl = userImgUrl;
        this.title = title;
        this.content = content;
        this.imgUrl = imgUrl;
        this.category = category;
        this.views = views;
        this.commentsCount = commentsCount;
        this.likeCount = likeCount;
        this.reviewSize = reviewSize;
        this.totalRating = totalRating;
        this.averageRating = averageRating;
        this.tags = tags;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
