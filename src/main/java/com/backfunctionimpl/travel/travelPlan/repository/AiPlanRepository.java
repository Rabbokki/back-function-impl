package com.backfunctionimpl.travel.travelPlan.repository;

import com.backfunctionimpl.travel.travelPlan.entity.AiPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AiPlanRepository extends JpaRepository<AiPlan,Long> {
    List<AiPlan> findByUserId(String userId);
}
