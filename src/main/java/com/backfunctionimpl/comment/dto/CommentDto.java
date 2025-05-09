package com.backfunctionimpl.comment.dto;


import com.backfunctionimpl.account.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommentDto extends BaseEntity {
    private Long id;
    private String email;
    private String content;
    private String author;
    private int likeCount;
    private String authorImage;

}