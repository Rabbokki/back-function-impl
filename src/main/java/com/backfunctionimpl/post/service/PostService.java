package com.backfunctionimpl.post.service;

import com.backfunctionimpl.post.dto.PostDto;
import com.backfunctionimpl.post.entity.Post;
import com.backfunctionimpl.post.entity.PostImage;
import com.backfunctionimpl.post.entity.PostTag;
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
                .build();

        // 이미지 매핑
        List<PostImage> images = dto.getImages().stream()
                .map(url -> PostImage.builder()
                        .imageUrl(url)
                        .post(post) // PostImage가 어떤 Post에 연결되는지 지정
                        .build())
                .toList();
        post.setImages(images);

        // 태그 매핑
        List<PostTag> tags = dto.getTag().stream()
                .map(tag -> PostTag.builder()
                        .tagName(tag)
                        .post(post)
                        .build())
                .toList();
        post.setTags(tags);

        // 저장
        Post saved = postRepository.save(post);

        // Post -> PostDto로 변환
        return new PostDto(
                saved.getId(),
                saved.getBoard(),
                saved.getTitle(),
                saved.getContent(),
                saved.getImages().stream().map(PostImage::getImageUrl).toList(),
                saved.getTags().stream().map(PostTag::getTagName).toList()
        );
    }


    public List<PostDto> getAllPosts() {
        return postRepository.findAll().stream()
                .map(post -> new PostDto(
                        post.getId(),
                        post.getBoard(),
                        post.getTitle(),
                        post.getContent(),
                        post.getImages().stream().map(PostImage::getImageUrl).toList(),
                        post.getTags().stream().map(PostTag::getTagName).toList()
                ))
                .toList();
    }



}
