package com.backfunctionimpl.like.repository;

import com.backfunctionimpl.account.entity.Account;
import com.backfunctionimpl.like.entity.CommentLike;
import com.backfunctionimpl.comment.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {
    boolean existsByAccountAndComment(Account account, Comment comment);
    Optional<CommentLike> findByAccountAndComment(Account account, Comment comment);
}
