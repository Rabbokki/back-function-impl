//package com.backfunctionimpl.review.dto;
//
//
//import com.backfunctionimpl.review.entity.Review;
//import jakarta.persistence.GeneratedValue;
//import jakarta.persistence.GenerationType;
//import jakarta.persistence.Id;
//import lombok.Getter;
//import lombok.NoArgsConstructor;
//import lombok.Setter;
//
//@Getter
//@Setter
//@NoArgsConstructor
//public class ReviewDTO {
//    private Long id;
//    private Long postId;
//    private Long accountId;
//    private int rating;
//    private String nickname;
//    private String content;
//
//    public ReviewDTO(Review review) {
//        this.id = review.getId();
//        this.postId = review.getPost().getId();
//        this.accountId = review.getAccount().getId();
//        this.rating = review.getRating();
//        this.nickname = review.getAccount().getNickname();
//        this.content = review.getContent();
//    }
//}
