package com.backfunctionimpl.like.repository;

import com.backfunctionimpl.account.entity.Account;
import com.backfunctionimpl.like.entity.Like;
import com.backfunctionimpl.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {
    boolean existsByAccountAndPost(Account account, Post post);
    Optional<Like> findByAccountAndPost(Account account, Post post);
}
