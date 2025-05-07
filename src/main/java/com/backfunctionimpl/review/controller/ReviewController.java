package com.backfunctionimpl.review.controller;

import com.backfunctionimpl.account.entity.Account;
import com.backfunctionimpl.account.repository.AccountRepository;
import com.backfunctionimpl.global.dto.ResponseDto;
import com.backfunctionimpl.global.security.user.UserDetailsImpl;
import com.backfunctionimpl.review.dto.ReviewDto;
import com.backfunctionimpl.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/reviews")
public class ReviewController {
    private final AccountRepository accountRepository;
    private final ReviewService reviewService;

    @GetMapping("/{postId}")
    public ResponseDto<?> getReviewsByPostId(@PathVariable("postId") Long postId) {
        return reviewService.getReviewsByPostId(postId);
    }

    @PostMapping("/{postId}")
    public ResponseDto<?> addOrUpdateReview(@PathVariable("postId") Long postId,
                                            @RequestBody ReviewDto reviewDTO,
                                            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        System.out.println("Received reviewDTO: " + reviewDTO);
        if (reviewDTO.getAccountId() == null) {
            return ResponseDto.fail("400", "Account ID is required");
        }

        Account account = accountRepository.findById(reviewDTO.getAccountId())
                .orElseThrow(() -> new RuntimeException("Account not found"));

        return reviewService.addOrUpdateReview(postId, account, reviewDTO.getRating(), reviewDTO.getContent());
    }

    @DeleteMapping("/{postId}")
    public ResponseDto<?> removeReview(@PathVariable("postId") Long postId,
                                       @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return reviewService.removeReview(postId, userDetails.getAccount());
    }

}
//package com.backfunctionimpl.review.controller;
//
//import com.backendfunction.account.entity.Account;
//import com.backendfunction.account.repository.AccountRepository;
//import com.backendfunction.global.dto.ResponseDto;
//import com.backendfunction.global.security.user.UserDetailsImpl;
//import com.backendfunction.review.dto.ReviewDTO;
//import com.backendfunction.review.service.ReviewService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.security.core.annotation.AuthenticationPrincipal;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequiredArgsConstructor
//@RequestMapping("/reviews")
//public class ReviewController {
//    private final AccountRepository accountRepository;
//    private final ReviewService reviewService;
//
//    @GetMapping("/{postId}")
//    public ResponseDto<?> getReviewsByPostId(@PathVariable("postId") Long postId) {
//        return reviewService.getReviewsByPostId(postId);
//    }
//
//    @PostMapping("/{postId}")
//    public ResponseDto<?> addOrUpdateReview(@PathVariable("postId") Long postId,
//                                            @RequestBody ReviewDTO reviewDTO,
//                                            @AuthenticationPrincipal UserDetailsImpl userDetails) {
//        System.out.println("Received reviewDTO: " + reviewDTO);
//        if (reviewDTO.getAccountId() == null) {
//            return ResponseDto.fail("400", "Account ID is required");
//        }
//
//        Account account = accountRepository.findById(reviewDTO.getAccountId())
//                .orElseThrow(() -> new RuntimeException("Account not found"));
//
//        return reviewService.addOrUpdateReview(postId, account, reviewDTO.getRating(), reviewDTO.getContent());
//    }
//
//    @DeleteMapping("/{postId}")
//    public ResponseDto<?> removeReview(@PathVariable("postId") Long postId,
//                                       @AuthenticationPrincipal UserDetailsImpl userDetails) {
//        return reviewService.removeReview(postId, userDetails.getAccount());
//    }
//
//}
