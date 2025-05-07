package com.backfunctionimpl.travel.travelFlight.dto;

import lombok.Data;

import java.util.List;

@Data
public class FlightSearchData {
    private List<FlightInfo> flights;
}