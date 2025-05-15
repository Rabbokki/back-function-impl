package com.backfunctionimpl.travel.travelPlan.service;

import com.backfunctionimpl.travel.travelPlan.dto.AiPlanResponseDto;
import com.backfunctionimpl.travel.travelPlan.entity.AiPlan;
import com.backfunctionimpl.travel.travelPlan.repository.AiPlanRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AiPlanService {
    private static final Logger logger = LoggerFactory.getLogger(AiPlanService.class);
    private final AiPlanRepository aiPlanRepository;
    private final ObjectMapper objectMapper;

    public AiPlanService(AiPlanRepository aiPlanRepository, ObjectMapper objectMapper) {
        this.aiPlanRepository = aiPlanRepository;
        this.objectMapper = objectMapper;
    }

    public void saveAiPlan(String userId, String destination, String startDate, String endDate, List<Map<String, Object>> itinerary, String planType) {
        try {
            AiPlan aiPlan = new AiPlan();
            aiPlan.setUserId(userId);
            aiPlan.setDestination(destination);
            aiPlan.setItineraryData(objectMapper.writeValueAsString(itinerary));

            DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;
            try {
                aiPlan.setStartDate(LocalDate.parse(startDate, formatter));
            } catch (DateTimeParseException e) {
                logger.warn("Invalid startDate format: {}. Using current date.", startDate);
                aiPlan.setStartDate(LocalDate.now());
            }
            try {
                aiPlan.setEndDate(LocalDate.parse(endDate, formatter));
            } catch (DateTimeParseException e) {
                logger.warn("Invalid endDate format: {}. Using startDate or current date.", endDate);
                aiPlan.setEndDate(aiPlan.getStartDate() != null ? aiPlan.getStartDate() : LocalDate.now());
            }

            // planType 유효성 검사
            if (planType == null || (!planType.equals("MY") && !planType.equals("AI"))) {
                logger.warn("Invalid planType: {}. Defaulting to 'MY'.", planType);
                aiPlan.setPlanType("MY");
            } else {
                aiPlan.setPlanType(planType);
                logger.info("Setting planType to: {}", planType);
            }

            aiPlan.setCreatedAt(LocalDateTime.now());
            aiPlanRepository.save(aiPlan);
            logger.info("AI plan saved successfully for user: {}, destination: {}, planType: {}", userId, destination, planType);
        } catch (Exception e) {
            logger.error("Error saving AI plan: {}", e.getMessage());
            throw new RuntimeException("일정 저장 중 오류 발생: " + e.getMessage());
        }
    }

    public List<AiPlanResponseDto> findAiPlansByUserId(String userId) {
        List<AiPlan> plans = aiPlanRepository.findByUserId(userId);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate now = LocalDate.now();

        return plans.stream().map(plan -> {
            AiPlanResponseDto dto = new AiPlanResponseDto();
            dto.setId(plan.getId());
            dto.setDestination(plan.getDestination());
            String startDate = plan.getStartDate() != null
                    ? plan.getStartDate().format(formatter)
                    : now.format(formatter);
            String endDate = plan.getEndDate() != null
                    ? plan.getEndDate().format(formatter)
                    : now.format(formatter);
            dto.setStartDate(startDate);
            dto.setEndDate(endDate);
            try {
                LocalDate endLocalDate = LocalDate.parse(endDate, formatter);
                dto.setStatus(now.isAfter(endLocalDate) ? "완료" : "예정");
            } catch (Exception e) {
                dto.setStatus("예정");
            }
            dto.setPlanType(plan.getPlanType() != null ? plan.getPlanType() : "MY");
            logger.debug("Returning planType: {} for plan ID: {}", dto.getPlanType(), plan.getId());
            return dto;
        }).collect(Collectors.toList());
    }
    private String extractDate(Object dayField, DateTimeFormatter formatter, int itinerarySize) {
        LocalDate baseDate = LocalDate.now(); // 기준 날짜 (임시)
        if (dayField == null) {
            return baseDate.format(formatter);
        }
        String dayStr = dayField.toString();
        // "day1", "day2" 같은 형식 처리
        if (dayStr.matches("day\\d+")) {
            int dayNum = Integer.parseInt(dayStr.replace("day", ""));
            return baseDate.plusDays(dayNum - 1).format(formatter);
        }
        // 이미 날짜 형식인 경우 (yyyy-MM-dd) 검증
        try {
            LocalDate parsedDate = LocalDate.parse(dayStr, formatter);
            return parsedDate.format(formatter);
        } catch (DateTimeParseException e) {
            // 잘못된 형식은 기본 날짜로 대체
            return baseDate.format(formatter);
        }
    }
}