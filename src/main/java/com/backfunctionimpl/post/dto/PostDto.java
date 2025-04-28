package com.backfunctionimpl.post.dto;

import com.backfunctionimpl.post.entity.PostImage;
import com.backfunctionimpl.post.entity.PostTag;
import com.backfunctionimpl.post.enums.Category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostDto {
    private Long id;
    private String title;
    private String content;
    private List<String> imgUrl; // 수정
    private Category category;
    private int views;
    private int commentsCount;
    private int likeCount;
    private List<String> tags;

    public PostDto(Long id, String title, String content, List<String> imgUrl, List<String> tags) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.imgUrl = imgUrl;
        this.tags = tags;
    }
}
