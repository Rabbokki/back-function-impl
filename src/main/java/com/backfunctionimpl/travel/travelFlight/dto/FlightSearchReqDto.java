package com.backfunctionimpl.travel.travelFlight.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class FlightSearchReqDto {
    private String origin;
    private String destination;
    private String departureDate;
}
