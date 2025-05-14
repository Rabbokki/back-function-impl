package com.backfunctionimpl.travel.travelFlight.repository;

import com.backfunctionimpl.travel.travelFlight.entity.AccountFlight;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AccountFlightRepository extends JpaRepository<AccountFlight, Long> {
    List<AccountFlight> findByAccountId(Long accountId);
}