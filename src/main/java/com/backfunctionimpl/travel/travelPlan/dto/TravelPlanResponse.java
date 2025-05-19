package com.backfunctionimpl.travel.travelPlan.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class TravelPlanResponse {
    private Long id;
    private String destination;
    private String startDate;
    private String endDate;
    private String status;
    private String planType;
}
