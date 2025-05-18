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
    @PostMapping("/save-initial")
    public ResponseEntity<String> saveInitialTravelPlan(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody Map<String, Object> planData) {
        logger.info("Saving initial travel plan for user: {}", userDetails.getUsername());
        try {
            String city = (String) planData.get("destination");
            String startDate = (String) planData.get("start_date");
            String endDate = (String) planData.get("end_date");
            String planType = (String) planData.get("planType");

            if (city == null || city.trim().isEmpty()) {
                throw new IllegalArgumentException("도시명이 누락되었습니다.");
            }
            if (startDate == null || startDate.trim().isEmpty()) {
                throw new IllegalArgumentException("시작 날짜가 누락되었습니다.");
            }
            if (endDate == null || endDate.trim().isEmpty()) {
                throw new IllegalArgumentException("종료 날짜가 누락되었습니다.");
            }

            // TravelPlanService로 전달할 데이터 준비
            Map<String, Object> formattedPlanData = new HashMap<>();
            formattedPlanData.put("city", city);
            formattedPlanData.put("country", getCountryByCity(city));
            formattedPlanData.put("start_date", startDate);
            formattedPlanData.put("end_date", endDate);
            formattedPlanData.put("planType", planType != null ? planType : "MY");
            formattedPlanData.put("places", new ArrayList<>());
            formattedPlanData.put("accommodations", new ArrayList<>());
            formattedPlanData.put("transportations", Collections.singletonList(
                    new HashMap<String, Object>() {{
                        put("type", "CAR");
                        put("day", startDate);
                    }}
            ));

            travelPlanService.saveTravelPlan(userDetails.getUsername(), formattedPlanData);
            return ResponseEntity.ok("초기 여행 계획 저장 성공");
        } catch (Exception e) {
            logger.error("Error saving initial travel plan: {}", e.getMessage());
            return ResponseEntity.status(400).body("초기 여행 계획 저장 실패: " + e.getMessage());
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
