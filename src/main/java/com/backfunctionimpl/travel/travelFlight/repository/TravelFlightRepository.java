package com.backfunctionimpl.travel.travelFlight.repository;

import com.backfunctionimpl.travel.travelFlight.entity.TravelFlight;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TravelFlightRepository extends JpaRepository<TravelFlight, Long> {
    Optional<TravelFlight> findByFlightId(String flightId);
    Optional<TravelFlight> findById(Long id);
}
