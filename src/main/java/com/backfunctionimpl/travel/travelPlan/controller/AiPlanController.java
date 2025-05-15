package com.backfunctionimpl.travel.travelPlan.controller;

import com.backfunctionimpl.travel.travelPlan.dto.AiPlanRequest;
import com.backfunctionimpl.travel.travelPlan.dto.AiPlanResponse;
import com.backfunctionimpl.travel.travelPlan.dto.AiPlanResponseDto;
import com.backfunctionimpl.travel.travelPlan.dto.AiPlanSaveRequest;
import com.backfunctionimpl.travel.travelPlan.service.AiPlanService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/aiplan")
public class AiPlanController {
    private static final Logger logger = LoggerFactory.getLogger(AiPlanController.class);

    private final AiPlanService aiPlanService;
    private final RestTemplate restTemplate;

    @Value("${fastapi.api-key}")
    private String fastApiKey;

    public AiPlanController(AiPlanService aiPlanService, RestTemplate restTemplate) {
        this.aiPlanService = aiPlanService;
        this.restTemplate = restTemplate;
    }

    @PostMapping("/generate")
    public ResponseEntity<AiPlanResponse> generateAiPlan(
            @RequestBody AiPlanRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        logger.info("Generating AI plan for user: {}, destination: {}", userDetails.getUsername(), request.getDestination());
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + fastApiKey);
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
            HttpEntity<AiPlanRequest> entity = new HttpEntity<>(request, headers);

            ResponseEntity<AiPlanResponse> response = restTemplate.exchange(
                    "http://localhost:8000/api/generate-itinerary",
                    HttpMethod.POST,
                    entity,
                    AiPlanResponse.class
            );

            logger.info("Received response from FastAPI: {}", response.getBody());
            return ResponseEntity.ok(response.getBody());
        } catch (HttpClientErrorException e) {
            logger.error("FastAPI error: Status {}, Response: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("AI 일정 생성 중 오류: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Error generating AI plan: {}", e.getMessage());
            throw new RuntimeException("AI 일정 생성 중 오류: " + e.getMessage());
        }
    }

    @PostMapping("/save")
    public ResponseEntity<String> saveAiPlan(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, Object> planData) {
        logger.info("Saving AI plan for user: {}", userDetails.getUsername());
        try {
            String destination = (String) planData.get("destination");
            String startDate = (String) planData.get("start_date");
            String endDate = (String) planData.get("end_date");
            String planType = (String) planData.get("planType");
            Object itineraryObj = planData.get("itinerary");

            // 입력 검증
            if (destination == null || destination.trim().isEmpty()) {
                throw new IllegalArgumentException("목적지가 누락되었습니다.");
            }
            if (itineraryObj == null) {
                throw new IllegalArgumentException("여행 일정 데이터가 누락되었습니다.");
            }

            // itinerary 처리
            List<Map<String, Object>> itinerary;
            if (itineraryObj instanceof List) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> list = (List<Map<String, Object>>) itineraryObj;
                itinerary = list;
            } else if (itineraryObj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> map = (Map<String, Object>) itineraryObj;
                itinerary = new ArrayList<>();
                for (String key : map.keySet()) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> dayData = (Map<String, Object>) map.get(key);
                    dayData.put("day", key);
                    itinerary.add(dayData);
                }
            } else {
                throw new IllegalArgumentException("itinerary 형식이 올바르지 않습니다. List 또는 Map이어야 합니다.");
            }

            if (itinerary.isEmpty()) {
                throw new IllegalArgumentException("여행 일정 데이터가 비어 있습니다.");
            }

            // startDate와 endDate 처리
            startDate = (startDate == null || startDate.trim().isEmpty()) ? LocalDate.now().toString() : startDate;
            endDate = (endDate == null || endDate.trim().isEmpty()) ? startDate : endDate;

            // planType 디버깅
            logger.info("Received planType: {}", planType);
            if (planType == null || (!planType.equals("MY") && !planType.equals("AI"))) {
                logger.warn("Invalid or missing planType: {}. Defaulting to 'MY'.", planType);
                planType = "MY";
            }

            aiPlanService.saveAiPlan(userDetails.getUsername(), destination, startDate, endDate, itinerary, planType);
            logger.info("AI plan save request processed with planType: {}", planType);
            return ResponseEntity.ok("AI 일정 저장 성공");
        } catch (Exception e) {
            logger.error("Error saving AI plan: {}", e.getMessage());
            throw new RuntimeException("AI 일정 저장 중 오류: " + e.getMessage());
        }
    }

    @GetMapping("/my-plans")
    public ResponseEntity<List<AiPlanResponseDto>> getMyAiPlans(
            @AuthenticationPrincipal UserDetails userDetails) {
        logger.info("Fetching AI plans for user: {}", userDetails.getUsername());
        try {
            List<AiPlanResponseDto> plans = aiPlanService.findAiPlansByUserId(userDetails.getUsername());
            logger.info("Fetched {} plans for user: {}", plans.size(), userDetails.getUsername());
            return ResponseEntity.ok(plans);
        } catch (Exception e) {
            logger.error("Error fetching AI plans: {}", e.getMessage());
            throw new RuntimeException("AI 일정 조회 중 오류: " + e.getMessage());
        }
    }
}
