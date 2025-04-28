package com.backfunctionimpl.post.service;

import com.backfunctionimpl.post.dto.PostDto;
import com.backfunctionimpl.post.entity.Post;
import com.backfunctionimpl.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;

    public PostDto createPost(PostDto dto) {
        Post post = Post.builder()
                .title(dto.getTitle())
                .content(dto.getContent())
                .imgUrl(dto.getImgUrl()) // 수정
                .category(dto.getCategory())
                .views(dto.getViews())
                .commentsCount(dto.getCommentsCount())
                .likeCount(dto.getLikeCount())
                .build();
        Post saved = postRepository.save(post);

        return PostDto.builder()
                .id(saved.getId())
                .title(saved.getTitle())
                .content(saved.getContent())
                .imgUrl(saved.getImgUrl()) // 수정
                .category(saved.getCategory())
                .views(saved.getViews())
                .commentsCount(saved.getCommentsCount())
                .likeCount(saved.getLikeCount())
                .build();
    }

    public List<PostDto> getAllPosts() {
        return postRepository.findAll().stream()
                .map(post -> PostDto.builder()
                        .id(post.getId())
                        .title(post.getTitle())
                        .content(post.getContent())
                        .imgUrl(post.getImgUrl()) // 수정
                        .category(post.getCategory())
                        .views(post.getViews())
                        .commentsCount(post.getCommentsCount())
                        .likeCount(post.getLikeCount())
                        .build())
                .collect(Collectors.toList());
    }
}
