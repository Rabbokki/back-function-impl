package com.backfunctionimpl.travel.travelPlace.entity;

import com.backfunctionimpl.account.entity.BaseEntity;
import com.backfunctionimpl.travel.travelPlan.entity.TravelPlan;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "travel_places")
@Getter
@Setter
public class TravelPlace extends BaseEntity {
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

    @Column(name = "time")
    private LocalTime time;

    @Column(name = "category")
    private String category;

    @Column(name = "description")
    private String description;

    @Column(name = "latitude")
    private double lat;

    @Column(name = "longitude")
    private double lng;
}
