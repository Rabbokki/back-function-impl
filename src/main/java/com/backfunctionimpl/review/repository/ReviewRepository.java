package com.backfunctionimpl.review.repository;

import com.backfunctionimpl.account.entity.Account;
import com.backfunctionimpl.post.entity.Post;
import com.backfunctionimpl.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByPost(Post post);
    Optional<Review> findByPostAndAccount(Post post, Account account);
//    boolean existsByPostAndAccount(Post post, Account account);
//    void deleteByPostAndAccount(Post post, Account account);
}