package com.backfunctionimpl.post.dto;

import com.backfunctionimpl.post.enums.Category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@Builder
public class PostDto {
    private Long id;
    private String title;
    private String content;
    private List<String> imgUrl;
    private Category category;
    private int views;
    private int commentsCount;
    private int likeCount;
    private List<String> tags;

    // 모든 필드를 받는 생성자 추가
    public PostDto(Long id, String title, String content, List<String> imgUrl, Category category,
                   int views, int commentsCount, int likeCount, List<String> tags) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.imgUrl = imgUrl;
        this.category = category;
        this.views = views;
        this.commentsCount = commentsCount;
        this.likeCount = likeCount;
        this.tags = tags;
    }
}
