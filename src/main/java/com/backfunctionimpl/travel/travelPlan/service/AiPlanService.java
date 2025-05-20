package com.backfunctionimpl.travel.travelPlan.service;

import com.backfunctionimpl.account.entity.Account;
import com.backfunctionimpl.account.repository.AccountRepository;
import com.backfunctionimpl.travel.travelPlan.dto.AiPlanRequest;
import com.backfunctionimpl.travel.travelPlan.dto.AiPlanResponse;
import com.backfunctionimpl.travel.travelPlan.dto.AiPlanResponseDto;
import com.backfunctionimpl.travel.travelPlan.entity.AiPlan;
import com.backfunctionimpl.travel.travelPlan.repository.AiPlanRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AiPlanService {
    private static final Logger logger = LoggerFactory.getLogger(AiPlanService.class);
    private final AiPlanRepository aiPlanRepository;
    private final AccountRepository accountRepository;
    private final ObjectMapper objectMapper;

    public AiPlanService(AiPlanRepository aiPlanRepository, AccountRepository accountRepository, ObjectMapper objectMapper) {
        this.aiPlanRepository = aiPlanRepository;
        this.accountRepository = accountRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public AiPlanResponse saveAiPlan(AiPlanRequest request, String userId) {
        try {
            logger.info("Received AI plan request: destination={}, startDate={}, endDate={}, userId={}",
                    request.getDestination(), request.getStartDate(), request.getEndDate(), userId);

            Account account = accountRepository.findByEmail(userId)
                    .orElseThrow(() -> new RuntimeException("User not found: " + userId));

            LocalDate startDate = parseDate(request.getStartDate(), "startDate");
            LocalDate endDate = parseDate(request.getEndDate(), "endDate");

            String itineraryJson = objectMapper.writeValueAsString(request.getItinerary());

            List<AiPlan> existingPlans = aiPlanRepository.findByUserIdAndDestinationAndStartDateAndEndDate(
                    userId, request.getDestination(), startDate, endDate);
            AiPlan aiPlan;
            if (!existingPlans.isEmpty()) {
                logger.warn("Duplicate AI plan found for user: {}, destination: {}, startDate: {}, endDate: {}",
                        userId, request.getDestination(), startDate, endDate);
                aiPlan = existingPlans.get(0);
            } else {
                aiPlan = AiPlan.builder()
                        .userId(userId)
                        .destination(request.getDestination())
                        .startDate(startDate)
                        .endDate(endDate)
                        .itineraryData(itineraryJson)
                        .planType("AI")
                        .build();
            }

            aiPlan.setItineraryData(itineraryJson);
            AiPlan savedPlan = aiPlanRepository.save(aiPlan);
            logger.info("AI plan saved successfully for user: {}, destination: {}", userId, savedPlan.getDestination());

            List<Map<String, Object>> itineraryResponse = objectMapper.readValue(
                    savedPlan.getItineraryData(),
                    new TypeReference<List<Map<String, Object>>>() {}
            );

            return AiPlanResponse.builder()
                    .id(savedPlan.getId())
                    .destination(savedPlan.getDestination())
                    .startDate(savedPlan.getStartDate().format(DateTimeFormatter.ISO_LOCAL_DATE))
                    .endDate(savedPlan.getEndDate().format(DateTimeFormatter.ISO_LOCAL_DATE))
                    .itinerary(itineraryResponse)
                    .build();
        } catch (Exception e) {
            logger.error("Error saving AI plan: {}", e.getMessage(), e);
            throw new RuntimeException("AI 계획 저장 중 오류 발생: " + e.getMessage());
        }
    }

    @Transactional
    public AiPlanResponse generateAiPlan(AiPlanRequest request, String userId) {
        try {
            logger.info("Generating AI plan for user: {}, destination: {}, startDate: {}, endDate: {}",
                    userId, request.getDestination(), request.getStartDate(), request.getEndDate());

            Account account = accountRepository.findByEmail(userId)
                    .orElseThrow(() -> new RuntimeException("User not found: " + userId));

            LocalDate startDate = parseDate(request.getStartDate(), "startDate");
            LocalDate endDate = parseDate(request.getEndDate(), "endDate");

            // 중복 일정 확인
            List<AiPlan> existingPlans = aiPlanRepository.findByUserIdAndDestinationAndStartDateAndEndDate(
                    userId, request.getDestination(), startDate, endDate);
            if (!existingPlans.isEmpty()) {
                logger.warn("Duplicate AI plan found for user: {}, destination: {}, startDate: {}, endDate: {}",
                        userId, request.getDestination(), startDate, endDate);
                AiPlan existingPlan = existingPlans.get(0);
                List<Map<String, Object>> itineraryResponse = objectMapper.readValue(
                        existingPlan.getItineraryData(),
                        new TypeReference<List<Map<String, Object>>>() {}
                );
                return AiPlanResponse.builder()
                        .id(existingPlan.getId())
                        .destination(existingPlan.getDestination())
                        .startDate(existingPlan.getStartDate().format(DateTimeFormatter.ISO_LOCAL_DATE))
                        .endDate(existingPlan.getEndDate().format(DateTimeFormatter.ISO_LOCAL_DATE))
                        .itinerary(itineraryResponse)
                        .build();
            }

            if (request.getItinerary() == null || request.getItinerary().isEmpty()) {
                logger.warn("Itinerary is null or empty, generating dummy itinerary");
                request.setItinerary(generateDummyItinerary(request.getDestination(), startDate, endDate));
            }

            String itineraryJson = objectMapper.writeValueAsString(request.getItinerary());

            AiPlan aiPlan = AiPlan.builder()
                    .userId(userId)
                    .destination(request.getDestination())
                    .startDate(startDate)
                    .endDate(endDate)
                    .itineraryData(itineraryJson)
                    .planType("AI")
                    .build();

            AiPlan savedPlan = aiPlanRepository.save(aiPlan);
            logger.info("AI plan generated and saved successfully for user: {}, destination: {}", userId, savedPlan.getDestination());

            List<Map<String, Object>> itineraryResponse = objectMapper.readValue(
                    savedPlan.getItineraryData(),
                    new TypeReference<List<Map<String, Object>>>() {}
            );

            return AiPlanResponse.builder()
                    .id(savedPlan.getId())
                    .destination(savedPlan.getDestination())
                    .startDate(savedPlan.getStartDate().format(DateTimeFormatter.ISO_LOCAL_DATE))
                    .endDate(savedPlan.getEndDate().format(DateTimeFormatter.ISO_LOCAL_DATE))
                    .itinerary(itineraryResponse)
                    .build();
        } catch (Exception e) {
            logger.error("Error generating AI plan: {}", e.getMessage(), e);
            throw new RuntimeException("AI 계획 생성 중 오류 발생: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public List<AiPlanResponse> getAiPlansByUserId(String userId) {
        try {
            List<AiPlan> plans = aiPlanRepository.findByUserId(userId);
            DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;
            LocalDate now = LocalDate.now();

            return plans.stream().map(plan -> {
                try {
                    List<Map<String, Object>> itinerary = objectMapper.readValue(
                            plan.getItineraryData(),
                            new TypeReference<List<Map<String, Object>>>() {}
                    );
                    AiPlanResponse response = AiPlanResponse.builder()
                            .id(plan.getId())
                            .destination(plan.getDestination())
                            .startDate(plan.getStartDate().format(formatter))
                            .endDate(plan.getEndDate().format(formatter))
                            .itinerary(itinerary)
                            .build();
                    LocalDate endDate = LocalDate.parse(response.getEndDate(), formatter);
                    response.setStatus(now.isAfter(endDate) ? "완료" : "예정");
                    return response;
                } catch (Exception e) {
                    logger.warn("Error parsing itinerary for plan id: {}. Error: {}", plan.getId(), e.getMessage());
                    return AiPlanResponse.builder()
                            .id(plan.getId())
                            .destination(plan.getDestination())
                            .startDate(plan.getStartDate().format(formatter))
                            .endDate(plan.getEndDate().format(formatter))
                            .itinerary(List.of())
                            .status("예정")
                            .build();
                }
            }).collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error fetching AI plans: {}", e.getMessage(), e);
            throw new RuntimeException("AI 계획 조회 중 오류: " + e.getMessage());
        }
    }

    private LocalDate parseDate(String dateStr, String fieldName) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            logger.warn("Missing {}: {}. Using current date.", fieldName, dateStr);
            return LocalDate.now();
        }
        try {
            return LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException e) {
            logger.warn("Invalid {} format: {}. Using current date.", fieldName, dateStr);
            return LocalDate.now();
        }
    }

    private List<Map<String, Object>> generateDummyItinerary(String destination, LocalDate startDate, LocalDate endDate) {
        long days = startDate.until(endDate).getDays() + 1;
        List<Map<String, Object>> itinerary = new ArrayList<>();

        Map<String, Object> day1 = new HashMap<>();
        day1.put("day", 1);
        List<Map<String, Object>> day1Activities = List.of(
                Map.of("activity", "City Tour", "description", "Explore the main attractions of " + destination),
                Map.of("activity", "Local Market", "description", "Visit a traditional market in " + destination)
        );
        day1.put("activities", day1Activities);
        itinerary.add(day1);

        if (days >= 2) {
            Map<String, Object> day2 = new HashMap<>();
            day2.put("day", 2);
            List<Map<String, Object>> day2Activities = List.of(
                    Map.of("activity", "Museum Visit", "description", "Discover the history of " + destination),
                    Map.of("activity", "Evening Walk", "description", "Enjoy the night scenery of " + destination)
            );
            day2.put("activities", day2Activities);
            itinerary.add(day2);
        }

        return itinerary.subList(0, Math.min((int) days, 2));
    }
}