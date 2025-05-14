package com.backfunctionimpl.travel.travelPlace.repository;

import com.backfunctionimpl.account.entity.Account;
import com.backfunctionimpl.travel.travelPlace.entity.SavedPlace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SavedPlaceRepository extends JpaRepository<SavedPlace, Long> {
    List<SavedPlace> findAllByAccount(Account account);
}