package com.backfunctionimpl.account.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.util.ArrayList;

@Entity
@Table(name = "account")
@Getter
@Setter
@ToString
@NoArgsConstructor
public class Account extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_id")
    private Long id;
    private String email;
    private String password;
    private String nickname;

    private LocalDate birthday;

    private String imgUrl;

    @Column(nullable = true)
    private Long kakaoId;

    @Column(nullable = true)
    private String provider;
    @Column(nullable = true)
    private String providerId;


    //일반 회원
//    public Account(AccountReqDto accountReqDto) {
//        this.email = accountReqDto.getEmail();
//        this.password = accountReqDto.getPassword();
//        this.nickname = accountReqDto.getNickname();
//        this.birthday = accountReqDto.getBirthday();
//        this.imgUrl = accountReqDto.getImgUrl();
//    }

    //google 로그인
    public Account(String email, String nickname, String provider, String providerId) {
        this.email = email;
        this.nickname = nickname;
        this.provider = provider;
        this.providerId = providerId;
    }
}