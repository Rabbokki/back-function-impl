package com.backfunctionimpl.review.dto;


import com.backfunctionimpl.review.entity.Review;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReviewSummaryDto {
    private Long id;
    private int rating;
    private String nickname;
    private String content;
    private LocalDateTime createdAt;

    public ReviewSummaryDto(Review review) {
        this.id = review.getId();
        this.rating = review.getRating();
        this.nickname = review.getAccount() != null ? review.getAccount().getNickname() : "알 수 없음";
        this.content = review.getContent();
        this.createdAt = review.getCreatedAt();
    }
}

