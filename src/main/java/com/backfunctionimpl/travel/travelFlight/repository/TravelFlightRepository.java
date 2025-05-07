package com.backfunctionimpl.travel.travelFlight.repository;

import com.backfunctionimpl.travel.travelFlight.entity.TravelFlight;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TravelFlightRepository extends JpaRepository<TravelFlight, Long> {
}
