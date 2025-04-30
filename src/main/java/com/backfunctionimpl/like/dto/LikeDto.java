package com.backfunctionimpl.like.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LikeDto {
    private Long id;
    private Long postId;
    private Long accountId;
}
