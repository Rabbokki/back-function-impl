package com.backfunctionimpl.travel.travelPlan.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;


@Getter
@Setter
@Builder
public class AiPlanResponse {
    private Long id;
    private String destination;
    private String startDate;
    private String endDate;
    private List<Map<String, Object>> itinerary;
    private String status;
}