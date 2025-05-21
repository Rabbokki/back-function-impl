package com.backfunctionimpl.travel.payment.entity;

import com.backfunctionimpl.account.entity.BaseEntity;
import com.backfunctionimpl.travel.travelFlight.entity.TravelFlight;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Data
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flight_id")
    private TravelFlight flight;

    private String tid;
    private String partnerOrderId;
    private String partnerUserId;
    private String status;
    private BigDecimal totalAmount;
    private Integer passengerCount;
    private String seats;
    private String contactEmail;
    private String contactPhone;
}
