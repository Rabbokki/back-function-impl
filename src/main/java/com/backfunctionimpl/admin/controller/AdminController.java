package com.backfunctionimpl.admin.controller;


import com.backfunctionimpl.account.dto.AccountResponseDto;
import com.backfunctionimpl.admin.service.AdminService;
import com.backfunctionimpl.post.dto.PostDto;
import com.backfunctionimpl.review.dto.ReviewDto;
import com.backfunctionimpl.review.dto.ReviewSummaryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class AdminController {
    private final AdminService adminService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users")
    public ResponseEntity<List<AccountResponseDto>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/posts")
    public ResponseEntity<List<PostDto>> getAllPosts() {
        return ResponseEntity.ok(adminService.getAllPosts());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<Void> deletePost(@PathVariable Long postId) {
        adminService.deletePost(postId);
        return ResponseEntity.noContent().build();
    }

//    @PreAuthorize("hasRole('ADMIN')")
//    @GetMapping("/reviews")
//    public ResponseEntity<List<ReviewSummaryDto>> getAllReviews() {
//        return ResponseEntity.ok(adminService.getAllReviews());
//    }
//
//    @PreAuthorize("hasRole('ADMIN')")
//    @DeleteMapping("/reviews/{reviewId}")
//    public ResponseEntity<Void> deleteReview(@PathVariable Long reviewId) {
//        adminService.deleteReview(reviewId);
//        return ResponseEntity.noContent().build();
//    }

}
