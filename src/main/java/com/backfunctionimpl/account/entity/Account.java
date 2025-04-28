package com.backfunctionimpl.account.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

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

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
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
        this.password = "SOCIAL_LOGIN"; //소셜 로그인용 기본값
    }
}