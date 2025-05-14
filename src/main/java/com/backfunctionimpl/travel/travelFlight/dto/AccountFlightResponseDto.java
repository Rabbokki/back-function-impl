package com.backfunctionimpl.travel.travelFlight.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class AccountFlightResponseDto {
    private Long id;
    private String flightId;
    private String carrier;
    private String carrierCode;
    private String flightNumber;
    private String departureAirport;
    private String arrivalAirport;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    private String returnDepartureAirport;
    private String returnArrivalAirport;
    private LocalDateTime returnDepartureTime;
    private LocalDateTime returnArrivalTime;
    private Integer passengerCount;
    private List<String> selectedSeats;
    private Double totalPrice;
    private String status;
    private LocalDateTime createdAt;
}
