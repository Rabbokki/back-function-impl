package com.backfunctionimpl.travel.travelPlan.entity;

import com.backfunctionimpl.account.entity.Account;
import com.backfunctionimpl.account.entity.BaseEntity;
import com.backfunctionimpl.travel.travelAccommodation.entity.TravelAccommodation;
import com.backfunctionimpl.travel.travelFlight.entity.TravelFlight;
import com.backfunctionimpl.travel.travelPlace.entity.TravelPlace;
import com.backfunctionimpl.travel.travelTransportation.entity.TravelTransportation;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "travel_plans")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TravelPlan extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "country")
    private String country;

    @Column(name = "city")
    private String city;
    @Column(name = "plan_type", nullable = false, columnDefinition = "VARCHAR(10) DEFAULT 'MY'")
    private String planType;
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

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

}
