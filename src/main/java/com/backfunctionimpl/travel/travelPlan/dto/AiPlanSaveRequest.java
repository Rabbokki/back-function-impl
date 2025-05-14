package com.backfunctionimpl.travel.travelPlan.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class AiPlanSaveRequest {
    private String destination;
    private List<Map<String, Object>> itinerary;
}
