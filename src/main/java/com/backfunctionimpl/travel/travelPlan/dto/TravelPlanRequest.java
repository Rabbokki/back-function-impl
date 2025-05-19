package com.backfunctionimpl.travel.travelPlan.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
public class TravelPlanRequest {
    private String city;
    private String country;
    @JsonProperty("start_date")
    private String startDate;
    @JsonProperty("end_date")
    private String endDate;
    @JsonProperty("plan_type")
    private String planType;
    private Long travelPlanId;

    private List<PlaceDto> places = new ArrayList<>();
    private List<AccommodationDto> accommodations = new ArrayList<>();
    private List<TransportationDto> transportations = new ArrayList<>();
}
