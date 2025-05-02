package com.backfunctionimpl.travel.travelFlight.dto;

import lombok.Data;

@Data
public class FlightInfo {
    private String id;
    private String carrier; // 예: "대한항공"
    private String carrierCode; // 예: "KE"
    private String flightNumber; // 예: "703"
    private String departureAirport; // 예: "ICN"
    private String arrivalAirport; // 예: "NRT"
    private String departureTime; // 예: "2025-02-01T10:00:00"
    private String arrivalTime; // 예: "2025-02-01T12:30:00"
    private String duration; // 예: "PT2H30M"
    private String aircraft; // 예: "Boeing 787-9"
    private String price; // 예: "250000"
    private String currency; // 예: "KRW"
}
