package com.backfunctionimpl.comment.service;

import com.backfunctionimpl.account.entity.Account;
import com.backfunctionimpl.comment.dto.CommentReqDto;
import com.backfunctionimpl.comment.entity.Comment;
import com.backfunctionimpl.comment.repository.CommentRepository;
import com.backfunctionimpl.global.dto.ResponseDto;
import com.backfunctionimpl.post.entity.Post;
import com.backfunctionimpl.post.repository.PostRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;

    // 댓글 생성
    @Transactional
    public ResponseDto<?> insertComment(CommentReqDto dto, Long postId, Account currentAccount) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));

        Comment comment = new Comment(dto.getContent(), post, currentAccount);
        post.getCommentList().add(comment);  // 게시글에 댓글 추가
        commentRepository.save(comment);

        // 댓글 수 갱신
        post.commentUpdate(post.getCommentList().size());

        return ResponseDto.success("댓글 작성 완료");
    }

    // 댓글 조회 (전체)
    public List<CommentReqDto> findAll() {
        List<Comment> comments = commentRepository.findAll();
        return comments.stream().map(CommentReqDto::fromEntity).toList();
    }

    // 댓글 조회 (단일)
    public ResponseDto<?> findByCommentId(Long id) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("댓글을 찾을 수 없습니다."));

        return ResponseDto.success(CommentReqDto.fromEntity(comment));
    }

    // 댓글 수정
    @Transactional
    public ResponseDto<?> updateByCommentId(Long id, CommentReqDto dto, Account account) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("댓글을 찾을 수 없습니다."));

        if (comment.getAccount().getId().equals(account.getId())) {
            comment.setContent(dto.getContent());
            commentRepository.save(comment);
        } else {
            throw new RuntimeException("댓글 작성자가 아닙니다.");
        }
        return ResponseDto.success("댓글 수정 완료");
    }

    // 댓글 삭제
    @Transactional
    public ResponseDto<?> deleteByCommentId(Long id, Account account) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("댓글을 찾을 수 없습니다."));

        if (comment.getAccount().getId().equals(account.getId())) {
            Post post = comment.getPost();
            post.getCommentList().remove(comment);  // 게시글에서 댓글 제거
            post.commentUpdate(post.getCommentList().size());  // 댓글 수 갱신
            commentRepository.delete(comment);
        } else {
            throw new RuntimeException("댓글 작성자가 아닙니다.");
        }
        return ResponseDto.success("댓글 삭제 완료");
    }
}
