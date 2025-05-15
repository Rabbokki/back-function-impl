package com.backfunctionimpl.review.repository;

import com.backfunctionimpl.account.entity.Account;
import com.backfunctionimpl.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByPlaceId(String placeId); // (기존)


    @Query("SELECT r FROM Review r JOIN FETCH r.account WHERE r.placeId = :placeId")
    List<Review> findWithAccountByPlaceId(@Param("placeId") String placeId);

    Optional<Review> findByPlaceIdAndAccount(String placeId, Account account);
}