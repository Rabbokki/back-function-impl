package com.backfunctionimpl.travel.travelPlace.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class SavedPlaceResponseDto {
    private String placeId;
    private String name;
    private String city;
    private String country;
    private String image;
    private String type;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime createdAt;
}
