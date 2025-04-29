package com.backfunctionimpl.post.service;

import com.backfunctionimpl.post.dto.PostDto;
import com.backfunctionimpl.post.entity.Post;
import com.backfunctionimpl.post.entity.Image;
import com.backfunctionimpl.post.entity.PostTag;
import com.backfunctionimpl.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;

    public PostDto createPost(PostDto dto) {
        Post post = Post.builder()
                .title(dto.getTitle())
                .content(dto.getContent())
                .build();

        // 이미지 매핑
        List<Image> images = dto.getImgUrl().stream()
                .map(url -> Image.builder()
                        .imageUrl(url)  // 수정
                        .post(post)
                        .build())
                .toList();
        post.setImages(images);  // 수정

        // 태그 매핑
        List<PostTag> tags = dto.getTags().stream()
                .map(tag -> PostTag.builder()
                        .tagName(tag)
                        .post(post)
                        .build())
                .toList();
        post.setTags(tags);

        Post saved = postRepository.save(post);

        return new PostDto(
                saved.getId(),
                saved.getTitle(),
                saved.getContent(),
                saved.getImages().stream().map(Image::getImageUrl).toList(),  // 수정
                saved.getCategory(),
                saved.getViews(),
                saved.getCommentsCount(),
                saved.getLikeCount(),
                saved.getTags().stream().map(PostTag::getTagName).toList()
        );
    }

    public List<PostDto> getAllPosts() {
        return postRepository.findAll().stream()
                .map(post -> new PostDto(
                        post.getId(),
                        post.getTitle(),
                        post.getContent(),
                        post.getImages().stream().map(Image::getImageUrl).toList(), // 수정
                        post.getCategory(),
                        post.getViews(),
                        post.getCommentsCount(),
                        post.getLikeCount(),
                        post.getTags().stream().map(PostTag::getTagName).toList()
                ))
                .toList();
    }
}
