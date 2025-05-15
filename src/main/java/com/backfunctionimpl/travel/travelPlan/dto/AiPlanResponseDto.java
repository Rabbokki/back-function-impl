package com.backfunctionimpl.travel.travelPlan.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AiPlanResponseDto {
    private Long id;
    private String destination;
    private String startDate;
    private String endDate;
    private String status;
    private String planType;
}
