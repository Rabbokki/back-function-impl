package com.backfunctionimpl.comment.controller;

import com.backfunctionimpl.comment.service.CommentService;
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
@RequestMapping("/api/comment")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;
    private final PostService postService;

    @GetMapping({"/"})
    public ResponseEntity<?> commentAll() {
        commentService.findAll();
        return ResponseEntity.status(HttpStatus.OK).body("전체 조회");
    }

//    @GetMapping("/api/comment/{id}")
//    public ResponseEntity<?> commentSearch(@PathVariable("id") Long id) throws BadRequestException {
//        CommentDto findComment = getDto(id, "댓글조회 실패");
//        return ResponseEntity.status(HttpStatus.OK).body(findComment);
//    }


}