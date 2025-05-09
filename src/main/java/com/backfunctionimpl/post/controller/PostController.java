package com.backfunctionimpl.post.controller;

import com.backfunctionimpl.account.entity.Account;
import com.backfunctionimpl.global.security.user.UserDetailsImpl;
import com.backfunctionimpl.post.dto.PostDto;
import com.backfunctionimpl.post.service.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    // 게시글 생성
    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createPost(
            @RequestPart(value = "postImg", required = false) List<MultipartFile> imgs,
            @RequestPart(value = "dto") PostDto dto,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        if (userDetails == null || userDetails.getAccount() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "로그인이 필요합니다."));
        }
        Account account = userDetails.getAccount();
        try {
            PostDto response = postService.createPost(dto, imgs, account);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "게시물 생성 중 오류 발생: " + e.getMessage()));
        }
    }

    // 게시글 수정
    @PatchMapping(value = "/update/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updatePost(
            @PathVariable("id") Long id,
            @RequestPart(value = "postImg", required = false) List<MultipartFile> imgs,
            @RequestPart(value = "dto") PostDto dto,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        try {
            return postService.updateByPost(id, imgs, dto, userDetails.getAccount());
        } catch (Exception e) {
            log.error("Error updating post: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("업데이트 실패");
        }
    }

    // 게시글 삭제
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deletePost(@PathVariable("id") Long id,
                                        @AuthenticationPrincipal UserDetailsImpl userDetails) {
        postService.deleteByPostId(id, userDetails.getAccount());
        return ResponseEntity.status(HttpStatus.OK).body("삭제 성공");
    }

    // 게시글 조회
    @GetMapping
    public List<PostDto> getFilteredPosts(
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "search", required = false) String search) {

        return postService.getFilteredPosts(category, search);
    }


    // 게시글 상세 조회
    @GetMapping("/find/{id}")
    public ResponseEntity<?> findByPostId(@PathVariable("id") Long id) {
        PostDto dto = postService.findById(id);
        return ResponseEntity.status(HttpStatus.OK).body(dto);
    }

    // 좋아요 처리
    @PostMapping("/{id}/like")
    public ResponseEntity<?> likePost(@PathVariable("id") Long id, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        postService.likePost(id, userDetails.getAccount());
        return ResponseEntity.ok(Map.of("message", "좋아요 처리 완료"));
    }
}
