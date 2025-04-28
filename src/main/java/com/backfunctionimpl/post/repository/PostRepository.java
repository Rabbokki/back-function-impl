package com.backfunctionimpl.post.repository;

import com.backfunctionimpl.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {

}
