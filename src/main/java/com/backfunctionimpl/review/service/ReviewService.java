package com.backfunctionimpl.review.service;

import com.backfunctionimpl.account.entity.Account;
import com.backfunctionimpl.account.repository.AccountRepository;
import com.backfunctionimpl.global.dto.ResponseDto;
import com.backfunctionimpl.review.dto.ReviewDto;
import com.backfunctionimpl.review.dto.ReviewSummaryDto;
import com.backfunctionimpl.review.entity.Review;
import com.backfunctionimpl.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final AccountRepository accountRepository;

    // ✅ 1. placeId로 리뷰 목록 조회
    @Transactional(readOnly = true)
    public ResponseDto<?> getReviewsByPlaceId(String placeId) {
        List<Review> reviewEntities = reviewRepository.findWithAccountByPlaceId(placeId);

        // ✅ 여기 디버깅용 출력 추가
        System.out.println("💬 리뷰 수: " + reviewEntities.size());
        for (Review r : reviewEntities) {
            System.out.println("⭐ " + r.getAccount().getNickname() + ": " + r.getContent());
        }

        List<ReviewSummaryDto> reviews = reviewEntities.stream()
                .map(ReviewSummaryDto::new)
                .toList();
        return ResponseDto.success(reviews);
    }
    // ✅ 2. 리뷰 등록
    @Transactional
    public ResponseDto<?> addPlaceReview(ReviewDto dto, Account account) {
        Review review = new Review(
                dto.getPlaceId(),
                dto.getPlaceName(), // ✅ 명소 이름도 저장
                account,
                dto.getRating(),
                account.getNickname(),
                dto.getContent()
        );

        reviewRepository.save(review);

        account.addExp(20);
        accountRepository.save(account);

        return ResponseDto.success("리뷰가 등록되었습니다.");
    }

    // ✅ 3. 리뷰 삭제
    @Transactional
    public ResponseDto<?> removeReviewById(Long reviewId, Account account) {
        Optional<Review> optional = reviewRepository.findById(reviewId);

        if (optional.isEmpty()) {
            return ResponseDto.fail("404", "리뷰가 존재하지 않습니다.");
        }

        Review review = optional.get();

        if (!review.getAccount().getId().equals(account.getId())) {
            return ResponseDto.fail("403", "본인의 리뷰만 삭제할 수 있습니다.");
        }

        reviewRepository.delete(review);
        return ResponseDto.success(Map.of("deletedId", reviewId));
    }

}