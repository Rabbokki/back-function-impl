package com.backfunctionimpl.post.controller;

import com.backfunctionimpl.post.dto.PostDto;
import com.backfunctionimpl.post.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping
    public PostDto create(@RequestBody PostDto dto) {
        return postService.createPost(dto);
    }

    @GetMapping
    public List<PostDto> getAll() {
        return postService.getAllPosts();
    }
}
