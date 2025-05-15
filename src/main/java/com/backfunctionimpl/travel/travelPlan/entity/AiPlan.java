package com.backfunctionimpl.travel.travelPlan.entity;

import com.backfunctionimpl.account.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

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
    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Column(name = "plan_type", nullable = false, columnDefinition = "VARCHAR(10) DEFAULT 'MY'")
    private String planType;
}
