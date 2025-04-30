package com.backfunctionimpl.travel.travelPlan.entity;

import com.backfunctionimpl.account.entity.Account;
import com.backfunctionimpl.account.entity.BaseEntity;
import com.backfunctionimpl.travel.travelAccommodation.entity.TravelAccommodation;
import com.backfunctionimpl.travel.travelFlight.entity.TravelFlight;
import com.backfunctionimpl.travel.travelPlace.entity.TravelPlace;
import com.backfunctionimpl.travel.travelTransportation.entity.TravelTransportation;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
public class TravelPlan extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate startDate;
    private LocalDate endDate;
    private String country;
    private String city;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "a_id")
    private Account account;

    @OneToMany(fetch = FetchType.LAZY,mappedBy = "travelPlan", cascade = CascadeType.ALL)
    private List<TravelTransportation> travelTransportations = new ArrayList<>();

    @OneToMany(fetch = FetchType.LAZY,mappedBy = "travelPlan", cascade = CascadeType.ALL)
    private List<TravelPlace> travelPlaces = new ArrayList<>();

    @OneToMany(fetch = FetchType.LAZY,mappedBy = "travelPlan", cascade = CascadeType.ALL)
    private List<TravelFlight> travelFlights = new ArrayList<>();

    @OneToMany(fetch = FetchType.LAZY,mappedBy = "travelPlan", cascade = CascadeType.ALL)
    private List<TravelAccommodation> travelAccommodations = new ArrayList<>();
}
