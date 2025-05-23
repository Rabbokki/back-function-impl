package com.backfunctionimpl.travel.travelPlan.controller;

import com.backfunctionimpl.travel.travelPlan.dto.AiPlanRequest;
import com.backfunctionimpl.travel.travelPlan.dto.AiPlanResponse;
import com.backfunctionimpl.travel.travelPlan.dto.AiPlanResponseDto;
import com.backfunctionimpl.travel.travelPlan.dto.AiPlanSaveRequest;
import com.backfunctionimpl.travel.travelPlan.service.AiPlanService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/aiplan")
public class AiPlanController {
    private static final Logger logger = LoggerFactory.getLogger(AiPlanController.class);

    private final AiPlanService aiPlanService;
    private final RestTemplate restTemplate;

    @Value("${fastapi.url:http://ai:5000}")
    private String fastApiUrl;

    @Value("${fastapi.key}")
    private String fastApiKey;

    public AiPlanController(AiPlanService aiPlanService, RestTemplate restTemplate) {
        this.aiPlanService = aiPlanService;
        this.restTemplate = restTemplate;
    }

    @PostMapping("/generate")
    public ResponseEntity<AiPlanResponse> generateAiPlan(
            @RequestBody @Valid AiPlanRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        logger.info("Generating AI plan for user: {}, request: {}", userDetails.getUsername(), request);
        try {
            Map<String, Object> fastApiRequest = new HashMap<>();
            fastApiRequest.put("destination", request.getDestination());
            fastApiRequest.put("preferences", request.getPreferences().toString()); // FastAPI는 문자열 기대
            fastApiRequest.put("budget", request.getBudget());
            fastApiRequest.put("pace", request.getPace());
            fastApiRequest.put("start_date", request.getStartDate());
            fastApiRequest.put("end_date", request.getEndDate());

            logger.info("FastAPI request body: {}", fastApiRequest);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + fastApiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(fastApiRequest, headers);

            ResponseEntity<Map> fastApiResponse = restTemplate.exchange(
                    fastApiUrl + "/api/generate-itinerary",
                    HttpMethod.POST,
                    entity,
                    Map.class
            );

            Map<String, Object> responseBody = fastApiResponse.getBody();
            if (responseBody == null || !responseBody.containsKey("itinerary")) {
                logger.error("FastAPI returned null or invalid response: {}", responseBody);
                throw new RuntimeException("FastAPI에서 유효한 일정 데이터를 반환하지 않았습니다.");
            }

            List<Map<String, Object>> itinerary = (List<Map<String, Object>>) responseBody.get("itinerary");
            if (itinerary == null || itinerary.isEmpty()) {
                logger.error("FastAPI returned empty itinerary");
                throw new RuntimeException("FastAPI에서 빈 일정 데이터를 반환했습니다.");
            }

            logger.info("FastAPI itinerary response: {}", itinerary);

            request.setItinerary(itinerary);
            request.setUserId(userDetails.getUsername());

            AiPlanResponse savedPlan = aiPlanService.generateAiPlan(request, userDetails.getUsername());
            logger.info("AI plan generated and saved for user: {}, destination: {}, id: {}",
                    userDetails.getUsername(), savedPlan.getDestination(), savedPlan.getId());
            return ResponseEntity.ok(savedPlan);
        } catch (HttpClientErrorException e) {
            logger.error("FastAPI error: Status {}, Response: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("AI 일정 생성 중 FastAPI 오류: " + e.getResponseBodyAsString());
        } catch (Exception e) {
            logger.error("Error generating AI plan: {}", e.getMessage(), e);
            throw new RuntimeException("AI 일정 생성 중 오류: " + e.getMessage());
        }
    }

    @PostMapping("/save")
    public ResponseEntity<String> saveAiPlan(
            @RequestBody @Valid AiPlanRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        logger.info("Saving AI plan for user: {}, destination: {}", userDetails.getUsername(), request.getDestination());
        try {
            request.setUserId(userDetails.getUsername());
            aiPlanService.saveAiPlan(request, userDetails.getUsername());
            logger.info("AI plan saved successfully for user: {}", userDetails.getUsername());
            return ResponseEntity.ok("일정 저장 성공");
        } catch (Exception e) {
            logger.error("Error saving AI plan: {}", e.getMessage(), e);
            throw new RuntimeException("일정 저장 중 오류: " + e.getMessage());
        }
    }

    @GetMapping("/my-plans")
    public ResponseEntity<List<AiPlanResponse>> getMyAiPlans(
            @AuthenticationPrincipal UserDetails userDetails) {
        logger.info("Fetching AI plans for user: {}", userDetails.getUsername());
        try {
            List<AiPlanResponse> plans = aiPlanService.getAiPlansByUserId(userDetails.getUsername());
            logger.info("Fetched {} AI plans for user: {}", plans.size(), userDetails.getUsername());
            return ResponseEntity.ok(plans);
        } catch (Exception e) {
            logger.error("Error fetching AI plans: {}", e.getMessage(), e);
            throw new RuntimeException("일정 조회 중 오류: " + e.getMessage());
        }
    }
}