package com.backfunctionimpl.travel.travelPlace.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SimplePlaceDto {
    private String id;
    private String name;
    private String address;
    private Double rating;
    private String image;
    private String category;
    private String city;
    private String cityId;
    private int likes;
    private String placeId;
}
