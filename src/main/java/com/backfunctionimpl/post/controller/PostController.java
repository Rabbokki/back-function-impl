package com.backfunctionimpl.post.controller;

import com.backfunctionimpl.account.entity.Account;
import com.backfunctionimpl.global.security.user.UserDetailsImpl;
import com.backfunctionimpl.post.dto.PostDto;
import com.backfunctionimpl.post.service.PostService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
            @RequestPart(value = "remainImgUrl", required = false) String remainImgUrlsJson,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        try {
            List<String> remainImgUrls = null;
            if (remainImgUrlsJson != null && !remainImgUrlsJson.isEmpty()) {
                ObjectMapper objectMapper = new ObjectMapper();
                remainImgUrls = objectMapper.readValue(remainImgUrlsJson, new TypeReference<List<String>>() {});
            }
            return postService.updateByPost(id, imgs, dto, remainImgUrls, userDetails.getAccount());
        } catch (Exception e) {
            log.error("Error updating post: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("업데이트 실패");
        }
    }

    // 게시글 삭제
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deletePost(@PathVariable("id") Long id,
                                        @AuthenticationPrincipal UserDetailsImpl userDetails) {
        if (userDetails == null || userDetails.getAccount() == null) {
            return null;
        }
        Account account = userDetails.getAccount();
        postService.deleteByPostId(id, account.getId());
        return ResponseEntity.status(HttpStatus.OK).body("삭제 성공");
    }

    // 게시글 조회
    @GetMapping
    public List<PostDto> getFilteredPosts(
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "search", required = false) String search) {

        return postService.getFilteredPosts(category, search);
    }

    // 게시글 유저 아이디로 조회
    @GetMapping("/account/")
    public List<PostDto> getAllUserPosts(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        if (userDetails == null || userDetails.getAccount() == null) {
            return null;
        }
        Account account = userDetails.getAccount();
        return postService.getAllUserPosts(account.getId());
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
    // 시고 처리
    @PutMapping("/{id}/reportAdd")
    public ResponseEntity<?> reportPost(@PathVariable("id") Long id) {
        postService.addReport(id);
        return ResponseEntity.ok(Map.of("message", "신고 처리 완료"));
    }

    @PutMapping("/{id}/reportRemove")
    public ResponseEntity<?> unreportPost(@PathVariable("id") Long id) {
        postService.removeReport(id);
        return ResponseEntity.ok(Map.of("message", "신고 삭제 완료"));
    }

    // 뷰 처리
    @PutMapping("/{id}/view")
    public ResponseEntity<?> viewPost(@PathVariable("id") Long id) {
        postService.addView(id);
        return ResponseEntity.ok(Map.of("message", "좋아요 처리 완료"));
    }
}
