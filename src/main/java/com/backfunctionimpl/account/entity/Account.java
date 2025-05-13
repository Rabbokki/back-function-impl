package com.backfunctionimpl.account.entity;

import com.backfunctionimpl.post.entity.Post;
import com.backfunctionimpl.travel.travelFlight.entity.AccountFlight;
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
@NoArgsConstructor
public class Account extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_id")
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String role;

    @Column(nullable = false)
    private String nickname;

    @Column(nullable = false)
    private LocalDate birthday;

    @Column(nullable = false)
    private String password;

    private String provider;
    private String providerId;

    private String bio;

    @Column(nullable = false)
    private String gender;

    private String imgUrl;

    @Column(nullable = false)
    private boolean agreeTerms;

    @Column(nullable = false)
    private boolean agreeMarketing;

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Post> posts = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private TravelLevel level;     // 레벨 이름대신 TravelLevel Enum
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "account", cascade = CascadeType.ALL)
    private List<TravelPlan> travelPlans = new ArrayList<>();

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AccountFlight> accountFlights = new ArrayList<>();
    //  경험치
    @Column(nullable = false)
    private int levelExp = 0;  // 기본 0



    //  현재 레벨 계산
    public TravelLevel getLevel() {
        return TravelLevel.findByExp(this.levelExp);
    }

    //  경험치 추가 헬퍼
    public void addExp(int exp) {
        this.levelExp += exp;
    }

    // 구글 로그인 생성자
    public Account(String email, String nickname, String provider, String providerId) {
        this.email = email;
        this.nickname = nickname;
        this.provider = provider;
        this.providerId = providerId;
        this.password = "SOCIAL_LOGIN";
    }
}
