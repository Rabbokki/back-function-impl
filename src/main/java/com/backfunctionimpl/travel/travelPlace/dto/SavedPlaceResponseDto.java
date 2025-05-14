package com.backfunctionimpl.travel.travelPlace.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SavedPlaceResponseDto {
    private Long id;
    private String name;
    private String type;
    private String city;
    private String country;
    private String image;
    private String savedDate;
}
