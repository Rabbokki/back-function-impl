package com.backfunctionimpl.travel.travelPlan.dto;

import lombok.Data;

@Data
public class PlaceDto {
    private String name;
    private String address;
    private String day;
    private String category;
    private String description;
    private Double latitude;
    private Double longitude;
    private String time; // 추가
}