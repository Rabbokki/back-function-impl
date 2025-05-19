package com.backfunctionimpl.travel.travelPlan.controller;

import com.backfunctionimpl.global.security.user.UserDetailsImpl;
import com.backfunctionimpl.travel.travelPlan.dto.TravelPlanResponse;
import com.backfunctionimpl.travel.travelPlan.dto.TravelPlanRequest;
import com.backfunctionimpl.travel.travelPlan.service.TravelPlanService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/travel-plans")
public class TravelPlanController {
    private static final Logger logger = LoggerFactory.getLogger(TravelPlanController.class);
    private final TravelPlanService travelPlanService;

    public TravelPlanController(TravelPlanService travelPlanService) {
        this.travelPlanService = travelPlanService;
    }

    @PostMapping
    public ResponseEntity<TravelPlanResponse> saveTravelPlan(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody TravelPlanRequest request) {
        logger.info("Saving travel plan for user: {}", userDetails.getUsername());
        try {
            if (request.getCity() == null || request.getCity().trim().isEmpty()) {
                logger.error("도시명이 누락되었습니다.");
                throw new IllegalArgumentException("도시명이 누락되었습니다.");
            }
            TravelPlanResponse response = travelPlanService.saveTravelPlan(request, userDetails.getUsername());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error saving travel plan: {}", e.getMessage());
            throw new RuntimeException("여행 계획 저장 중 오류: " + e.getMessage());
        }
    }

    @GetMapping("/my-plans")
    public ResponseEntity<List<TravelPlanResponse>> getMyTravelPlans(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        logger.info("Fetching travel plans for user: {}", userDetails.getUsername());
        try {
            List<TravelPlanResponse> plans = travelPlanService.getPlansByUserId(userDetails.getUsername());
            logger.info("Fetched {} plans for user: {}", plans.size(), userDetails.getUsername());
            return ResponseEntity.ok(plans);
        } catch (Exception e) {
            logger.error("Error fetching travel plans: {}", e.getMessage());
            throw new RuntimeException("여행 계획 조회 중 오류: " + e.getMessage());
        }
    }

    // 도시별 국가 매핑
    private String getCountryByCity(String city) {
        Map<String, String> cityToCountry = new HashMap<>();
        cityToCountry.put("jeju", "한국");
        cityToCountry.put("bangkok", "태국");
        cityToCountry.put("fukuoka", "일본");
        cityToCountry.put("osaka", "일본");
        cityToCountry.put("paris", "프랑스");
        cityToCountry.put("rome", "이탈리아");
        cityToCountry.put("singapore", "싱가포르");
        cityToCountry.put("tokyo", "일본");
        cityToCountry.put("venice", "이탈리아");
        return cityToCountry.getOrDefault(city.toLowerCase(), "알 수 없음");
    }
}
