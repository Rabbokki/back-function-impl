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
        return likeService.addLike(postId, userDetails.getAccount());
    }

    @DeleteMapping({"/{postId}"})
    public ResponseDto<?> removeLike(@PathVariable("postId") Long postId,
                                     @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return likeService.removeLike(postId, userDetails.getAccount());
    }

    @GetMapping({"/status/{postId}"})
    public ResponseDto<?> getPostLikeStatus(@PathVariable("postId") Long postId,
                                     @AuthenticationPrincipal UserDetailsImpl userDetails) {
        boolean isLiked = likeService.isPostLiked(postId, userDetails.getAccount());
        return ResponseDto.success(isLiked);
    }


}
