package com.backfunctionimpl.like.service;

import com.backfunctionimpl.account.entity.Account;
import com.backfunctionimpl.comment.dto.CommentReqDto;
import com.backfunctionimpl.comment.entity.Comment;
import com.backfunctionimpl.global.dto.ResponseDto;
import com.backfunctionimpl.like.dto.LikeDto;
import com.backfunctionimpl.like.entity.Like;
import com.backfunctionimpl.like.repository.LikeRepository;
import com.backfunctionimpl.post.entity.Post;
import com.backfunctionimpl.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LikeService {

    private final LikeRepository likeRepository;
    private final PostRepository postRepository;

    public ResponseDto<?> addLike(Long postId, Account account) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("찜을 찾을 수 없습니다."));
        if (likeRepository.existsByAccountAndPost(account, post)) {
            return ResponseDto.fail("이미 좋차요 되있음.", "게시물이 좋아요 되있음.");
        }
        Like newLike = new Like(post, account);
        likeRepository.save(newLike);
        post.likeUpdate(+1);
        postRepository.save(post);
        return ResponseDto.success("찜 추가했습니다.");
    }

    public ResponseDto<?> removeLike(Long postId, Account account) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("포스트 없읍니다."));

        Like existingLike = likeRepository.findByAccountAndPost(account, post)
                .orElseThrow(() -> new IllegalArgumentException("포스트가 아직 찜 안했습니다."));

        likeRepository.delete(existingLike);
        post.likeUpdate(-1);
        postRepository.save(post);
        return ResponseDto.success("찜 제거했습니다.");
    }

    public boolean isPostLiked(Long postId, Account account) {
        return likeRepository.findByAccountAndPost(account, postRepository.findById(postId).orElse(null)).isPresent();
    }
}
