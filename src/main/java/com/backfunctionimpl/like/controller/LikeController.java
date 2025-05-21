package com.backfunctionimpl.like.controller;

import com.backfunctionimpl.account.repository.AccountRepository;
import com.backfunctionimpl.like.service.LikeService;
import com.backfunctionimpl.global.dto.ResponseDto;
import com.backfunctionimpl.global.security.user.UserDetailsImpl;
import com.backfunctionimpl.post.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/like")
@RequiredArgsConstructor
public class LikeController {
    private final LikeService likeService;

    @PostMapping({"/{postId}"})
    public ResponseDto<?> addLike(@PathVariable("postId") Long postId,
                                  @AuthenticationPrincipal UserDetailsImpl userDetails) {
        Long accountId = userDetails.getAccount().getId();
        return likeService.addLike(postId, accountId);
    }

    @PostMapping({"/comment/{commentId}"})
    public ResponseDto<?> addCommentLike(@PathVariable("commentId") Long commentId,
                                  @AuthenticationPrincipal UserDetailsImpl userDetails) {
        Long accountId = userDetails.getAccount().getId();
        return likeService.addCommentLike(commentId, accountId);
    }

    @DeleteMapping({"/{postId}"})
    public ResponseDto<?> removeLike(@PathVariable("postId") Long postId,
                                     @AuthenticationPrincipal UserDetailsImpl userDetails) {
        Long accountId = userDetails.getAccount().getId();
        return likeService.removeLike(postId, accountId);
    }

    @DeleteMapping({"/comment/{commentId}"})
    public ResponseDto<?> removeCommentLike(@PathVariable("commentId") Long commentId,
                                     @AuthenticationPrincipal UserDetailsImpl userDetails) {
        Long accountId = userDetails.getAccount().getId();
        return likeService.removeCommentLike(commentId, accountId);
    }

    @GetMapping({"/status/{postId}"})
    public ResponseDto<?> getPostLikeStatus(@PathVariable("postId") Long postId,
                                     @AuthenticationPrincipal UserDetailsImpl userDetails) {
        boolean isLiked = likeService.isPostLiked(postId, userDetails.getAccount());
        return ResponseDto.success(isLiked);
    }

    @GetMapping({"/commentStatus/{commentId}"})
    public ResponseDto<?> getCommentLikeStatus(@PathVariable("commentId") Long commentId,
                                            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        boolean isLiked = likeService.isCommentLiked(commentId, userDetails.getAccount());
        return ResponseDto.success(isLiked);
    }
}
