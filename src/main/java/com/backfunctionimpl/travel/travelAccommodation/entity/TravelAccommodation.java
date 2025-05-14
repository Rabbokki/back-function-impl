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
    private String day;
    private String description;
    private LocalDateTime checkInDate;
    private LocalDateTime checkOutDate;
    private double latitude;
    private double longitude;

    public void setDay(String day) {
        this.day = day;
    }

    public String getDay() {
        return this.day;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setLat(double lat) {
        this.latitude = lat;
    }

    public void setLng(double lng) {
        this.longitude = lng;
    }

    public void setTravelPlan(TravelPlan travelPlan) {
        this.travelPlan = travelPlan;
    }


}
