package com.backfunctionimpl.travel.travelFlight.entity;

import com.backfunctionimpl.account.entity.BaseEntity;
import com.backfunctionimpl.travel.travelPlan.entity.TravelPlan;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class TravelFlight extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "travel_plan_id")
    private TravelPlan travelPlan;

    private String airlines;
    private String flightNumber;
    private String departureAirport;
    private String arrivalAirport;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    @Column(name = "flight_id")
    private String flightId; // Amadeus flight ID 저장
    @Column(name = "price")
    private String price;

    @Column(name = "currency")
    private String currency;

    @Column(name = "cabin_baggage")
    private String cabinBaggage;
    // 귀국 여정 필드 추가
    private LocalDateTime returnDepartureTime;
    private LocalDateTime returnArrivalTime;
    private String returnDepartureAirport;
    private String returnArrivalAirport;
}
