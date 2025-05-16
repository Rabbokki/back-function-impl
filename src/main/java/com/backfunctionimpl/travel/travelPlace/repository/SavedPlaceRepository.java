package com.backfunctionimpl.travel.travelPlace.repository;

import com.backfunctionimpl.account.entity.Account;
import com.backfunctionimpl.travel.travelPlace.entity.SavedPlace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SavedPlaceRepository extends JpaRepository<SavedPlace, Long> {
    List<SavedPlace> findAllByAccountId(Long accountId);

    Optional<SavedPlace> findByPlaceIdAndAccount(String placeId, Account account);

    boolean existsByPlaceIdAndAccount(String placeId, Account account);
}