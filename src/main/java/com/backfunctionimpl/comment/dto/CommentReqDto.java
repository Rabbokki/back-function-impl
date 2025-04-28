package com.backfunctionimpl.comment.dto;

import com.backfunctionimpl.account.entity.Account;
import com.backfunctionimpl.comment.entity.Comment;
import com.backfunctionimpl.post.entity.Post;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor
@Getter
@AllArgsConstructor
public class CommentReqDto {
    private Long id;
    @NotBlank(message = "댓글을 입력해 주세요.")
    private String content;
    private int likeCount;

    public static CommentReqDto fromEntity(Comment comment){
        return new CommentReqDto(
                comment.getId(),
                comment.getContent(),
                comment.getLikeSize()
        );
    }

    public static Comment fromDto(CommentDto commentDto, Post post,
                                  Account account){
        return new Comment(
            commentDto.getContent(),
                post,
                account
        );
    }
}
