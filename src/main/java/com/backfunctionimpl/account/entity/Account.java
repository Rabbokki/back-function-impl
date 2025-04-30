package com.backfunctionimpl.account.entity;


import com.backfunctionimpl.post.entity.Post;

import com.backfunctionimpl.travel.travelPlan.entity.TravelPlan;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "account")
@Getter
@Setter
@ToString(exclude = "posts")
@NoArgsConstructor
public class Account extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_id")
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String nickname;

    @Column(nullable = false)
    private LocalDate birthday;

    @Column(nullable = false)
    private String password;

    private String imgUrl;

    @Column(nullable = true)
    private String provider;

    @Column(nullable = true)
    private String providerId;

    @OneToMany(mappedBy = "account",
            cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Post> posts = new ArrayList<>();



    @Enumerated(EnumType.STRING)
    private TravelLevel level;     // 레벨 이름대신 TravelLevel Enum

    private Integer levelExp;   // 경험치 퍼센트

    @OneToMany(fetch = FetchType.LAZY,mappedBy = "account", cascade = CascadeType.ALL)
    private List<TravelPlan> travelPlans = new ArrayList<>();


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