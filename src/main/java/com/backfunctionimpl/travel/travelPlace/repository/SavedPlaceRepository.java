package com.backfunctionimpl.travel.travelPlace.repository;

import com.backfunctionimpl.account.entity.Account;
import com.backfunctionimpl.travel.travelPlace.entity.SavedPlace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface SavedPlaceRepository extends JpaRepository<SavedPlace, Long> {
    List<SavedPlace> findAllByAccount(Account account);

    @Query("SELECT s FROM SavedPlace s WHERE s.id = :id AND s.account = :account")
    Optional<SavedPlace> findByIdAndAccount(@Param("id") Long id, @Param("account") Account account);

    @Modifying
    @Transactional
    @Query("DELETE FROM SavedPlace s WHERE s.id = :id AND s.account = :account")
    void deleteByIdAndAccount(@Param("id") Long id, @Param("account") Account account);

    boolean existsByAccountAndPlaceId(Account account, String placeId);
}
