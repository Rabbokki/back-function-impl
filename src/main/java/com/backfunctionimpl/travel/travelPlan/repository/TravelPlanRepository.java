package com.backfunctionimpl.travel.travelPlan.repository;

import com.backfunctionimpl.travel.travelPlan.entity.TravelPlan;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TravelPlanRepository extends JpaRepository<TravelPlan, Long> {
}
