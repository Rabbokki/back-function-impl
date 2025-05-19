package com.backfunctionimpl.travel.travelPlan.repository;

import com.backfunctionimpl.account.entity.Account;
import com.backfunctionimpl.travel.travelPlan.entity.TravelPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TravelPlanRepository extends JpaRepository<TravelPlan, Long> {

    Optional<TravelPlan> findFirstByOrderByIdDesc();
    List<TravelPlan> findByAccountId(Long accountId);
    List<TravelPlan> findByAccountEmail(String email);

    List<TravelPlan> findByAccountAndCityAndStartDateAndEndDateAndPlanType(Account account, String city, LocalDate startDate, LocalDate endDate, String planType);
}
