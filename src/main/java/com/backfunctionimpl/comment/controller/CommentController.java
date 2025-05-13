package com.backfunctionimpl.comment.controller;

import com.backfunctionimpl.comment.dto.CommentDto;
import com.backfunctionimpl.comment.dto.CommentReqDto;
import com.backfunctionimpl.comment.service.CommentService;
import com.backfunctionimpl.global.dto.ResponseDto;
import com.backfunctionimpl.global.security.user.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comment")
@RequiredArgsConstructor
@Slf4j
public class CommentController {

    private final CommentService commentService;

    // 댓글 생성
    @PostMapping("/posts/{postId}/comment")
    public ResponseDto<?> createComment(@PathVariable("postId") Long postId,
                                        @RequestBody CommentReqDto dto,
                                        @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return commentService.insertComment(dto, postId, userDetails.getAccount());
    }

    // 댓글 조회 (전체)
    @GetMapping
    public ResponseEntity<List<CommentReqDto>> getAllComments() {
        List<CommentReqDto> comments = commentService.findAll();
        return ResponseEntity.ok(comments);
    }

    // 댓글 조회 (단일)
    @GetMapping("/{id}")
    public ResponseDto<?> getComment(@PathVariable Long id) {
        return commentService.findByCommentId(id);
    }

    // 댓글 수정
    @PatchMapping("/update/{id}")
    public ResponseDto<?> updateComment(@PathVariable Long id,
                                        @RequestBody CommentReqDto dto,
                                        @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return commentService.updateByCommentId(id, dto, userDetails.getAccount());
    }

    // 댓글 삭제
    @DeleteMapping("/delete/{id}")
    public ResponseDto<?> deleteComment(@PathVariable Long id,
                                        @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return commentService.deleteByCommentId(id, userDetails.getAccount());
    }
    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<?> getCommentsByPostId(@PathVariable("postId") Long postId) {
        log.info("📣 getCommentsByPostId 컨트롤러 진입 - postId: {}", postId);
        List<CommentDto> comments = commentService.getCommentsByPostId(postId); // ← 이거 맞아?
        return ResponseEntity.ok(comments);
    }
}
