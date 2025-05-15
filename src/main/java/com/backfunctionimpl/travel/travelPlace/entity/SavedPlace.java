package com.backfunctionimpl.travel.travelPlace.entity;


import com.backfunctionimpl.account.entity.Account;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "saved_place")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class SavedPlace {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String placeId;
    private String name;
    private String city;
    private String country;
    @Column(length = 1000)
    private String image;
    private String type;

    private LocalDate savedDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Account account;

    @PrePersist
    public void setSavedDate() {
        this.savedDate = LocalDate.now();
    }
}