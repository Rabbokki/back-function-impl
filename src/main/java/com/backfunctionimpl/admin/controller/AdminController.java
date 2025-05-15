package com.backfunctionimpl.admin.controller;


import com.backfunctionimpl.account.dto.AccountResponseDto;
import com.backfunctionimpl.account.dto.AccountUpdateRequestDto;
import com.backfunctionimpl.account.entity.Account;
import com.backfunctionimpl.admin.service.AdminService;
import com.backfunctionimpl.global.security.user.UserDetailsImpl;
import com.backfunctionimpl.post.dto.PostDto;
import com.backfunctionimpl.post.service.PostService;
import com.backfunctionimpl.review.dto.ReviewDto;
import com.backfunctionimpl.review.dto.ReviewSummaryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class AdminController {
    private final AdminService adminService;
    private final PostService postService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users")
    public ResponseEntity<List<AccountResponseDto>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/users/update/{id}")
    public ResponseEntity<AccountResponseDto> updateUser(
            @PathVariable("id") Long id,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage,
            @RequestPart(value = "dto") AccountUpdateRequestDto updateDto) {

        AccountResponseDto updatedUser = adminService.updateUser(id, updateDto, profileImage);
        return ResponseEntity.ok(updatedUser);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/users/delete/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable("id") Long id) {
        adminService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users/posts/{id}")
    public List<PostDto> getAllUserPosts(@PathVariable("id") Long id) {
        return postService.getAllUserPosts(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/posts")
    public ResponseEntity<List<PostDto>> getAllPosts() {
        return ResponseEntity.ok(adminService.getAllPosts());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/posts/delete/{postId}")
    public ResponseEntity<Void> deletePost(
            @PathVariable("postId") Long postId,
            @RequestParam("accountId") Long accountId
    ) {
        postService.deleteByPostId(postId, accountId);
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
