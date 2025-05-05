package com.backfunctionimpl.travel.travelFlight.dto;

import lombok.Data;


@Data
public class FlightInfo {
    private String id;
    private String departureAirport;
    private String arrivalAirport;
    private String departureTime;
    private String arrivalTime;
    private String duration;
    private String price;
    private String currency;
    private String carrier;
    private String carrierCode;
    private String flightNumber;
    private String aircraft;
    private String cabinBaggage;
    private int numberOfBookableSeats;
}