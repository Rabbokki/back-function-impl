package com.backfunctionimpl.post.dto;

import com.backfunctionimpl.post.enums.Category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostDto {
    private Long id;
    private String title;
    private String content;
    private String imgUrl; // 수정
    private Category category;
    private int views;
    private int commentsCount;
    private int likeCount;
}
