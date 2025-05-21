package com.backfunctionimpl.travel.payment.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class PaymentRequest {
    private String flightId;
    private Integer passengerCount;
    private List<String> selectedSeats;
    private BigDecimal totalPrice;
    private List<Passenger> passengers;
    private Contact contact;

    @Data
    public static class Passenger {
        private String firstName;
        private String lastName;
        private String birthDate;
        private String gender;
        private String nationality;
        private String passportNumber;
    }

    @Data
    public static class Contact {
        private String email;
        private String phone;
    }
}
