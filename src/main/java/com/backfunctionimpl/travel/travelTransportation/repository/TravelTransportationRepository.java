package com.backfunctionimpl.travel.travelTransportation.repository;

import com.backfunctionimpl.travel.travelTransportation.entity.TravelTransportation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TravelTransportationRepository extends JpaRepository<TravelTransportation, Long> {
}
