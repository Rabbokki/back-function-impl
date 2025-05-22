package com.backfunctionimpl.travel.payment.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.regex.Pattern;

@Data
public class PaymentRequest {
    private String flightId;
    private Integer passengerCount;
    private List<String> selectedSeats;
    private BigDecimal totalPrice;
    private List<Passenger> passengers;
    private Contact contact;
    private String itemName; // 추가된 필드

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

        private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");

        public String getEmail() {
            if (email == null || email.trim().isEmpty() || !EMAIL_PATTERN.matcher(email).matches()) {
                return "user_" + System.currentTimeMillis() + "@example.com";
            }
            return email;
        }

        public String getPhone() {
            return phone != null && !phone.trim().isEmpty() ? phone : "01000000000";
        }
    }
}
