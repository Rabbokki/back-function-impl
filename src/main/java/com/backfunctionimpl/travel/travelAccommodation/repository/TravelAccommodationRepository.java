package com.backfunctionimpl.travel.travelAccommodation.repository;

import com.backfunctionimpl.travel.travelAccommodation.entity.TravelAccommodation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TravelAccommodationRepository extends JpaRepository<TravelAccommodation, Long> {
}
