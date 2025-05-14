package com.backfunctionimpl.travel.travelPlan.controller;

import com.backfunctionimpl.travel.travelPlan.dto.AiPlanRequest;
import com.backfunctionimpl.travel.travelPlan.dto.AiPlanResponse;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

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
            @RequestBody AiPlanSaveRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        logger.info("Saving AI plan for user: {}, destination: {}", userDetails.getUsername(), request.getDestination());
        try {
            aiPlanService.saveAiPlan(
                    userDetails.getUsername(),
                    request.getDestination(),
                    request.getItinerary()
            );
            return ResponseEntity.ok("일정이 저장되었습니다.");
        } catch (Exception e) {
            logger.error("Error saving AI plan: {}", e.getMessage());
            throw new RuntimeException("일정 저장 중 오류: " + e.getMessage());
        }
    }
}
