package com.backfunctionimpl.travel.travelAccommodation.entity;

import com.backfunctionimpl.account.entity.BaseEntity;
import com.backfunctionimpl.travel.travelPlan.entity.TravelPlan;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "travel_accommodations")
@Getter
@Setter
public class TravelAccommodation extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "travel_plan_id")
    private TravelPlan travelPlan;
    @Column(name = "name")
    private String name;

    @Column(name = "address")
    private String address;

    @Column(name = "day")
    private String day;

    @Column(name = "description")
    private String description;

    @Column(name = "check_in_date")
    private LocalDateTime checkInDate;

    @Column(name = "check_out_date")
    private LocalDateTime checkOutDate;

    @Column(name = "latitude")
    private double latitude;

    @Column(name = "longitude")
    private double longitude;
}
