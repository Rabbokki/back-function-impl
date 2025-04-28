package com.backfunctionimpl.comment.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentDto {
    private Long id;
    private String nickname;
    private String content;



//    public static CommentDto fromEntity(Comment comment) {
//        return new CommentDto(
//                comment.getId(),
//                comment.getNickname(),
//                comment.getContent()
//        );
//    }
//
//    public static Comment fromDto(CommentDto dto) {
//        Comment comment = new Comment();
//        comment.setId(dto.getId());
//        comment.setNickname(dto.getNickname());
//        comment.setContent(dto.getContent());
//        return comment;
//    }
}