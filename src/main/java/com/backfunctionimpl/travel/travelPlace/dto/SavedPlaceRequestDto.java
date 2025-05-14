package com.backfunctionimpl.travel.travelPlace.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SavedPlaceRequestDto {
    private String placeId;
    private String name;
    private String city;
    private String country;
    private String image;
    private String type;
}
