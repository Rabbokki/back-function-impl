package com.backfunctionimpl.travel.travelFlight.entity;

import com.backfunctionimpl.account.entity.Account;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "account_flight")
@Getter
@Setter
public class AccountFlight {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(name = "flight_id", nullable = false)
    private String flightId; // TravelFlight의 ID 참조

    @Column(name = "carrier", nullable = false)
    private String carrier;

    @Column(name = "carrier_code", nullable = false)
    private String carrierCode;

    @Column(name = "flight_number", nullable = false)
    private String flightNumber;

    @Column(name = "departure_airport", nullable = false)
    private String departureAirport;

    @Column(name = "arrival_airport", nullable = false)
    private String arrivalAirport;

    @Column(name = "departure_time", nullable = false)
    private LocalDateTime departureTime;

    @Column(name = "arrival_time", nullable = false)
    private LocalDateTime arrivalTime;

    @Column(name = "return_departure_airport")
    private String returnDepartureAirport;

    @Column(name = "return_arrival_airport")
    private String returnArrivalAirport;

    @Column(name = "return_departure_time")
    private LocalDateTime returnDepartureTime;

    @Column(name = "return_arrival_time")
    private LocalDateTime returnArrivalTime;

    @Column(name = "passenger_count", nullable = false)
    private Integer passengerCount;

    @Column(name = "selected_seats", length = 1000)
    private String selectedSeats; // JSON 문자열 또는 콤마로 구분된 문자열

    @Column(name = "total_price", nullable = false)
    private Double totalPrice;

    @Column(name = "status", nullable = false)
    private String status; // 예: RESERVED, CANCELLED

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
