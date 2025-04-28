package com.backfunctionimpl.post.service;

import com.backfunctionimpl.post.dto.PostDto;
import com.backfunctionimpl.post.entity.Post;
import com.backfunctionimpl.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {

    private  final PostRepository postRepository;

    public PostDto createPost(PostDto dto) {
        Post post = Post.builder()
                .board(dto.getBoard())
                .title(dto.getTitle())
                .content(dto.getContent())
                .images(dto.getImages())
                .tag(dto.getTag())
                .build();
        Post saved = postRepository.save(post);

        return new PostDto(
                saved.getId(),
                saved.getBoard(),
                saved.getTitle(),
                saved.getContent(),
                saved.getImages(),
                saved.getTag()
        );
    }

    public List<PostDto> getAllPosts() {
        return postRepository.findAll().stream()
                        .map(post -> new PostDto(
                                post.getId(),
                                post.getBoard(),
                                post.getTitle(),
                                post.getContent(),
                                post.getImages(),
                                post.getTag()
                        )).toList();
    }


}
