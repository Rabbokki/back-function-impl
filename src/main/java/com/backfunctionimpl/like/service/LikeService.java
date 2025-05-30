package com.backfunctionimpl.like.service;

import com.backfunctionimpl.account.entity.Account;
import com.backfunctionimpl.account.repository.AccountRepository;
import com.backfunctionimpl.comment.dto.CommentReqDto;
import com.backfunctionimpl.comment.entity.Comment;
import com.backfunctionimpl.comment.repository.CommentRepository;
import com.backfunctionimpl.global.dto.ResponseDto;
import com.backfunctionimpl.like.dto.LikeDto;
import com.backfunctionimpl.like.entity.CommentLike;
import com.backfunctionimpl.like.entity.Like;
import com.backfunctionimpl.like.repository.CommentLikeRepository;
import com.backfunctionimpl.like.repository.LikeRepository;
import com.backfunctionimpl.post.entity.Post;
import com.backfunctionimpl.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LikeService {

    private final LikeRepository likeRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final AccountRepository accountRepository;


    @Transactional
    public ResponseDto<?> addLike(Long postId, Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("유저 없음"));
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("포스트 없읍니다."));

        if (likeRepository.existsByAccountAndPost(account, post)) {
            return ResponseDto.fail("이미 좋차요 되있음.", "게시물이 좋아요 되있음.");
        }

        Like newLike = new Like(post, account);
        likeRepository.save(newLike);

        post.likeUpdate(+1);
        postRepository.save(post);

        return ResponseDto.success("찜 추가했습니다.");
    }

    @Transactional
    public ResponseDto<?> addCommentLike(Long commentId, Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("유저 없음"));
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("포스트 없읍니다."));

        if (commentLikeRepository.existsByAccountAndComment(account, comment)) {
            return ResponseDto.fail("이미 좋차요 되있음.", "게시물이 좋아요 되있음.");
        }

        CommentLike newCommentLike = new CommentLike(comment, account);
        commentLikeRepository.save(newCommentLike);

        comment.likeUpdate(+1);
        commentRepository.save(comment);

        return ResponseDto.success("찜 추가했습니다.");
    }

    @Transactional
    public ResponseDto<?> removeLike(Long postId, Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("유저 없음"));
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("포스트 없읍니다."));

        Like existingLike = likeRepository.findByAccountAndPost(account, post)
                .orElseThrow(() -> new IllegalArgumentException("포스트가 아직 찜 안했습니다."));

        likeRepository.delete(existingLike);
        post.likeUpdate(-1);
        postRepository.save(post);
        return ResponseDto.success("찜 제거했습니다.");
    }

    @Transactional
    public ResponseDto<?> removeCommentLike(Long commentId, Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("유저 없음"));
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("커맨트 없읍니다."));

        CommentLike existingCommentLike = commentLikeRepository.findByAccountAndComment(account, comment)
                .orElseThrow(() -> new IllegalArgumentException("커맨트가 아직 찜 안했습니다."));

        commentLikeRepository.delete(existingCommentLike);
        comment.likeUpdate(-1);
        commentRepository.save(comment);
        return ResponseDto.success("찜 제거했습니다.");
    }

    public boolean isPostLiked(Long postId, Account account) {
        return likeRepository.findByAccountAndPost(account, postRepository.findById(postId).orElse(null)).isPresent();
    }

    public boolean isCommentLiked(Long commentId, Account account) {
        return commentLikeRepository.findByAccountAndComment(account, commentRepository.findById(commentId).orElse(null)).isPresent();
    }
}
