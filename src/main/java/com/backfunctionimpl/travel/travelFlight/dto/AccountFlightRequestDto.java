package com.backfunctionimpl.travel.travelFlight.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class AccountFlightRequestDto {
    private String flightId;
    private String carrier;
    private String carrierCode;
    private String flightNumber;
    private String departureAirport;
    private String arrivalAirport;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime departureTime;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime arrivalTime;
    private String returnDepartureAirport;
    private String returnArrivalAirport;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime returnDepartureTime;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime returnArrivalTime;
    private Integer passengerCount;
    private List<String> selectedSeats;
    private Double totalPrice;
    private List<PassengerInfo> passengers;
    private ContactInfo contact;

    @Getter
    @Setter
    public static class PassengerInfo {
        private String firstName;
        private String lastName;
        private String birthDate;
        private String gender;
        private String nationality;
        private String passportNumber;
    }

    @Getter
    @Setter
    public static class ContactInfo {
        private String email;
        private String phone;
    }
}
