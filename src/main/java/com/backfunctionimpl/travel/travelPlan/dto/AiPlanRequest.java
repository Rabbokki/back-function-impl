package com.backfunctionimpl.travel.travelPlan.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AiPlanRequest {
    private String destination;
    private String preferences;
    private int budget;
    private int pace;
    private String start_date;
    private String end_date;
}
