package com.backfunctionimpl.travel.travelPlan.controller;

import com.backfunctionimpl.global.security.user.UserDetailsImpl;
import com.backfunctionimpl.travel.travelPlan.dto.TravelPlanResponseDto;
import com.backfunctionimpl.travel.travelPlan.dto.TravelPlanSaveRequestDto;
import com.backfunctionimpl.travel.travelPlan.service.TravelPlanService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/travel-plans")
public class TravelPlanController {
    private static final Logger logger = LoggerFactory.getLogger(TravelPlanController.class);
    private final TravelPlanService travelPlanService;

    public TravelPlanController(TravelPlanService travelPlanService) {
        this.travelPlanService = travelPlanService;
    }
    @PostMapping
    public ResponseEntity<String> saveTravelPlan(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody Map<String, Object> planData) {
        logger.info("Saving travel plan for user: {}", userDetails.getUsername());
        try {
            String city = (String) planData.get("city");
            if (city == null || city.trim().isEmpty()) {
                throw new IllegalArgumentException("도시명이 누락되었습니다.");
            }
            travelPlanService.saveTravelPlan(userDetails.getUsername(), planData);
            return ResponseEntity.ok("여행 계획 저장 성공");
        } catch (Exception e) {
            logger.error("Error saving travel plan: {}", e.getMessage());
            throw new RuntimeException("여행 계획 저장 중 오류: " + e.getMessage());
        }
    }

    @GetMapping("/my-plans")
    public ResponseEntity<List<TravelPlanResponseDto>> getMyTravelPlans(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        logger.info("Fetching travel plans for user: {}", userDetails.getUsername());
        try {
            List<TravelPlanResponseDto> plans = travelPlanService.findTravelPlansByUserId(userDetails.getUsername());
            logger.info("Fetched {} plans for user: {}", plans.size(), userDetails.getUsername());
            return ResponseEntity.ok(plans);
        } catch (Exception e) {
            logger.error("Error fetching travel plans: {}", e.getMessage());
            throw new RuntimeException("여행 계획 조회 중 오류: " + e.getMessage());
        }
    }
}
