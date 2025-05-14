package com.backfunctionimpl.travel.travelPlace.entity;

import com.backfunctionimpl.account.entity.BaseEntity;
import com.backfunctionimpl.travel.travelPlan.entity.TravelPlan;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
public class TravelPlace extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "travel_plan_id")
    private TravelPlan travelPlan;

    private String name;
    private String address;
    private String day;
    private LocalTime time;
    private String category;
    private String description;
    private double lat;
    private double lng;

    public void setDay(String day) { this.day = day; }
    public String getDay() { return this.day; }

    public void setName(String name) { this.name = name; }
    public void setCategory(String category) { this.category = category; }
    public void setDescription(String description) { this.description = description; }
    public void setTime(LocalTime time) { this.time = time; }
    public void setLat(double lat) { this.lat = lat; }
    public void setLng(double lng) { this.lng = lng; }
    public void setTravelPlan(TravelPlan travelPlan) { this.travelPlan = travelPlan; }



}
