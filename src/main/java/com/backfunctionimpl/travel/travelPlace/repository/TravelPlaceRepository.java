package com.backfunctionimpl.travel.travelPlace.repository;

import com.backfunctionimpl.travel.travelPlace.entity.TravelPlace;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TravelPlaceRepository extends JpaRepository<TravelPlace, Long> {
}
