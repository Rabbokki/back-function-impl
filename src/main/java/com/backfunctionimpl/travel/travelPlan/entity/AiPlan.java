package com.backfunctionimpl.travel.travelPlan.entity;

import com.backfunctionimpl.account.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "ai_plans")
@Getter
@Setter
public class AiPlan extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "destination", nullable = false)
    private String destination;

    @Column(name = "itinerary_data", columnDefinition = "JSON")
    private String itineraryData;
}
