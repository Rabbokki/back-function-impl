package com.backfunctionimpl.travel.travelFlight.dto;

import lombok.*;

import java.util.List;

@Data
public class FlightSearchResDto {
    private List<FlightInfo> flights;
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FlightInfo{
        private String airline;
        private String flightNumber;
        private String departureAirport;
        private String arrivalAirport;
        private String departureTime;
        private String arrivalTime;
        private String duration;
        private String price;
    }
}
