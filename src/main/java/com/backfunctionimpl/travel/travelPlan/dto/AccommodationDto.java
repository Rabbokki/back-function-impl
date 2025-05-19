package com.backfunctionimpl.travel.travelPlan.dto;

import lombok.Data;

@Data
public class AccommodationDto {
    private String name;
    private String address;
    private String day;
    private String description;
    private Double latitude;
    private Double longitude;
    private String checkInDate; // 추가
    private String checkOutDate; // 추가
}