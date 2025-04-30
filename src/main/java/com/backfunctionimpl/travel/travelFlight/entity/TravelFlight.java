package com.backfunctionimpl.travel.travelFlight.entity;

import com.backfunctionimpl.account.entity.BaseEntity;
import com.backfunctionimpl.travel.travelPlan.entity.TravelPlan;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
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
}
