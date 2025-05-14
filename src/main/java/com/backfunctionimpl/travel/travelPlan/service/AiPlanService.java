package com.backfunctionimpl.travel.travelPlan.service;

import com.backfunctionimpl.travel.travelPlan.entity.AiPlan;
import com.backfunctionimpl.travel.travelPlan.repository.AiPlanRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class AiPlanService {
    private final AiPlanRepository aiPlanRepository;
    private final ObjectMapper objectMapper;

    public AiPlanService(AiPlanRepository aiPlanRepository, ObjectMapper objectMapper) {
        this.aiPlanRepository = aiPlanRepository;
        this.objectMapper = objectMapper;
    }

    public void saveAiPlan(String userId, String destination, List<Map<String, Object>> itinerary) {
        try {
            AiPlan aiPlan = new AiPlan();
            aiPlan.setUserId(userId);
            aiPlan.setDestination(destination);
            aiPlan.setItineraryData(objectMapper.writeValueAsString(itinerary));
            aiPlan.setCreatedAt(LocalDateTime.now());
            aiPlanRepository.save(aiPlan);
        } catch (Exception e) {
            throw new RuntimeException("일정 저장 중 오류 발생: " + e.getMessage());
        }
    }
}