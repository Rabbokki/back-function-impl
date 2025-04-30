package com.backfunctionimpl.travel.travelAccommodation.entity;

import com.backfunctionimpl.account.entity.BaseEntity;
import com.backfunctionimpl.travel.travelPlan.entity.TravelPlan;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class TravelAccommodation extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "travel_plan_id")
    private TravelPlan travelPlan;

    private String name;
    private String address;
    private LocalDateTime checkInDate;
    private LocalDateTime checkOutDate;
    private double latitude;
    private double longitude;
}
