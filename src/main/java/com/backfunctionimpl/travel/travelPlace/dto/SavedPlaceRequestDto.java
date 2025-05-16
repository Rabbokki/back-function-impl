package com.backfunctionimpl.travel.travelPlace.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SavedPlaceRequestDto {
    @NotBlank(message = "placeId는 필수입니다.")
    private String placeId;
    private String name;
    private String city;
    private String country;
    private String image;
    private String type;
}
