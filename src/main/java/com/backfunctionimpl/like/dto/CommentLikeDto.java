package com.backfunctionimpl.like.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentLikeDto {
    private Long id;
    private Long commentId;
    private Long accountId;
}