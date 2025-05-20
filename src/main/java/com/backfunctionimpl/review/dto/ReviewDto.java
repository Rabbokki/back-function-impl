package com.backfunctionimpl.review.dto;

import com.backfunctionimpl.review.entity.Review;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ReviewDto {
    private Long id;
    private String placeId;
    private String placeName;
    private Long accountId;
    private int rating;
    private String nickname;
    private String content;

    public ReviewDto(Review review) {
        this.id = review.getId();
        this.placeId = review.getPlaceId();
        this.placeName = review.getPlaceName();
        this.accountId = review.getAccount().getId();
        this.rating = review.getRating();
        this.nickname = review.getNickname();
        this.content = review.getContent();
    }
}
