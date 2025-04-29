
package com.backfunctionimpl.comment.service;
import com.backfunctionimpl.comment.entity.Comment;


import com.backfunctionimpl.account.entity.Account;
import com.backfunctionimpl.comment.dto.CommentReqDto;
import com.backfunctionimpl.comment.repository.CommentRepository;
import com.backfunctionimpl.global.dto.ResponseDto;
import com.backfunctionimpl.post.entity.Post;
import com.backfunctionimpl.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;

    public List<CommentReqDto> findAll() {
        List<Comment> comments = commentRepository.findAll();
        return comments.stream().map(CommentReqDto::fromEntity).toList();
    }

    public ResponseDto<?> findByCommentId(Long id) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("댓글을 찾을 수 없습니다."));

        Map<String, Object> data = new HashMap<>();
        data.put("postId", comment.getPost().getId());
        data.put("dto", CommentReqDto.fromEntity(comment));

        return ResponseDto.success(data);
    }


    public ResponseDto<?> insertComment(CommentReqDto dto, Long postId, Account currentAccount) {
        Post post = postRepository.findById(postId).orElseThrow(()->
                new RuntimeException("댓글 작성 실패"));
        Comment comment = new Comment(dto.getContent(),post,currentAccount);
        post.getCommentList().add(comment);
        commentRepository.save(comment);
        post.commentUpdate(post.getCommentList().size());
        return ResponseDto.success("댓글 작성 완료");
    }

    public ResponseDto<?> updateByCommentId(Long id,CommentReqDto dto, Account account) {
        Comment comment = commentRepository.findById(id).orElseThrow(
                ()-> new RuntimeException("댓글 수정 실패"));
        if(comment.getAccount().getId().equals(account.getId())){
            comment.setContent(dto.getContent());
            commentRepository.save(comment);
        }else {
            throw new RuntimeException("댓글 작성자 아님");
        }
        return ResponseDto.success("댓글 수정 성공");
    }

    public ResponseDto<?> deleteByCommentId(Long id, Account account) {
        Comment comment = commentRepository.findById(id).orElseThrow(()->
                new RuntimeException("댓글 삭제 실패"));
        Post post = comment.getPost();

        if(comment.getAccount().getId().equals(account.getId())){
            post.getCommentList().remove(comment);
            post.commentUpdate(-1);
            commentRepository.delete(comment);
        }else {
            throw new RuntimeException("댓글 작성자 아님");
        }
        return ResponseDto.success("댓글 삭제 성공");
    }
}
