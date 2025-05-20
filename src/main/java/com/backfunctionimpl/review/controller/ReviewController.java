package com.backfunctionimpl.review.controller;

import com.backfunctionimpl.account.entity.Account;
import com.backfunctionimpl.account.repository.AccountRepository;
import com.backfunctionimpl.global.dto.ResponseDto;
import com.backfunctionimpl.global.security.user.UserDetailsImpl;
import com.backfunctionimpl.review.dto.ReviewDto;
import com.backfunctionimpl.review.dto.ReviewSummaryDto;
import com.backfunctionimpl.review.entity.Review;
import com.backfunctionimpl.review.repository.ReviewRepository;
import com.backfunctionimpl.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;
    private final AccountRepository accountRepository;
    private final ReviewRepository reviewRepository;

    // ✅ 1. placeId로 리뷰 목록 조회
    @GetMapping
    public ResponseDto<?> getReviews(@RequestParam("placeId") String placeId) {
        System.out.println("📥 받은 placeId: " + placeId);
        ResponseDto<?> response = reviewService.getReviewsByPlaceId(placeId);
        System.out.println("📤 반환할 리뷰: " + response); // 여기에 찍히는 값 확인
        return response;
    }


    // ✅ 2. 리뷰 작성
    @PostMapping
    public ResponseDto<?> addReview(@RequestBody ReviewDto dto) {
        Account account = accountRepository.findById(dto.getAccountId())
                .orElseThrow(() -> new RuntimeException("Account not found"));
        return reviewService.addPlaceReview(dto, account);
    }

    // ✅ 3. 본인 리뷰 삭제
    @DeleteMapping
    public ResponseDto<?> deleteReview(@RequestParam("reviewId") Long reviewId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseDto.fail("401", "로그인이 필요합니다.");
        }

        String email = auth.getName();
        Account account = accountRepository.findByEmail(email).orElse(null);
        if (account == null) {
            return ResponseDto.fail("404", "회원 정보를 찾을 수 없습니다.");
        }

        return reviewService.removeReviewById(reviewId, account);
    }

    // ✅ GET /api/reviews/me - 로그인한 사용자의 리뷰만 조회
    @GetMapping("/me")
    public ResponseDto<?> getMyReviews(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseDto.fail("401", "로그인이 필요합니다.");
        }

        String email = userDetails.getUsername();
        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));

        List<Review> myReviews = reviewRepository.findByAccount(account);
        List<ReviewSummaryDto> response = myReviews.stream()
                .map(ReviewSummaryDto::new)
                .toList();

        return ResponseDto.success(response);
    }




}