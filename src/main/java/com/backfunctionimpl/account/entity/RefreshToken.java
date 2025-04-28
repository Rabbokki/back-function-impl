package com.backfunctionimpl.account.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "refreshToken_id")
    private Long id;
    @NotBlank
    private String refreshToken;
    @NotBlank
    private String accountEmail;

    @Builder
    public RefreshToken(String refreshToken, String accountEmail) {
        this.refreshToken = refreshToken;
        this.accountEmail = accountEmail;
    }


    public RefreshToken updateToken(String token){
        this.refreshToken = token;
        return this;
    }
}