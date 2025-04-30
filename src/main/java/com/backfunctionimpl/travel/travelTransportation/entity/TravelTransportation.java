package com.backfunctionimpl.travel.travelTransportation.entity;

import com.backfunctionimpl.account.entity.Account;
import com.backfunctionimpl.account.entity.BaseEntity;
import com.backfunctionimpl.travel.travelPlan.entity.TravelPlan;
import com.backfunctionimpl.travel.travelTransportation.enums.Type;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class TravelTransportation extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private Type type;
    private String startLocation;
    private String endLocation;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "travel_plan_id")
    private TravelPlan travelPlan;

}
