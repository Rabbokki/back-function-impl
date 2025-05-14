package com.backfunctionimpl.review.controller;

import com.backfunctionimpl.account.entity.Account;
import com.backfunctionimpl.account.repository.AccountRepository;
import com.backfunctionimpl.global.dto.ResponseDto;
import com.backfunctionimpl.global.security.user.UserDetailsImpl;
import com.backfunctionimpl.review.dto.ReviewDto;
import com.backfunctionimpl.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;
    private final AccountRepository accountRepository;

    // ✅ 1. placeId로 리뷰 목록 조회
    @GetMapping
    public ResponseDto<?> getReviews(@RequestParam("placeId") String placeId) {
        System.out.println("📥 받은 placeId: " + placeId);
        return reviewService.getReviewsByPlaceId(placeId);
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
    public ResponseDto<?> deleteReview(@RequestParam("placeId") String placeId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseDto.fail("401", "로그인이 필요합니다.");
        }

        String email = authentication.getName(); // email 또는 username
        Account account = accountRepository.findByEmail(email).orElse(null);

        if (account == null) {
            return ResponseDto.fail("404", "회원 정보를 찾을 수 없습니다.");
        }

        return reviewService.removeReview(placeId, account);
    }

}
