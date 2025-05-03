package com.backfunctionimpl.travel.travelFlight.dto;

import lombok.Data;

@Data
public class FlightInfo {
    private String id;
    private String price;
    private String currency;
    private String carrierCode;
    private String carrier;
    private String flightNumber;
    private String departureAirport;
    private String arrivalAirport;
    private String departureTime;
    private String arrivalTime;
    private String duration;
    private String aircraft;
    private int numberOfBookableSeats;
    private String cabinBaggage;
    // Getters and setters
}