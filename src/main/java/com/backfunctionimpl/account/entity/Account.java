package com.backfunctionimpl.account.entity;

import com.backfunctionimpl.post.entity.Post;
import com.backfunctionimpl.review.entity.Review;
import com.backfunctionimpl.travel.travelFlight.entity.AccountFlight;
import com.backfunctionimpl.travel.travelPlace.entity.SavedPlace;
import com.backfunctionimpl.travel.travelPlan.entity.TravelPlan;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "account")
@Getter
@Setter
@NoArgsConstructor
public class Account extends BaseEntity {
    //카카오

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_id")
    private Long id;

    @Column(nullable = true)
    private String email;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String nickname;

    @Column(nullable = true) // 생일이 없을 수도 있으므로 null 허용
    private LocalDate birthday;

    @Column(nullable = false)
    private String password;

    private String provider;
    private String providerId;

    @Column(nullable = false)
    private String role;

    @Column(nullable = true)
    private String bio;

    @Column(nullable = true)
    private String gender;

    private String imgUrl;

    @Column(nullable = false)
    private boolean agreeTerms;

    @Column(nullable = false)
    private boolean agreeMarketing;

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Post> posts = new ArrayList<>();

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL)
    private List<Review> reviews;

    @Enumerated(EnumType.STRING)
    private TravelLevel level;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "account", cascade = CascadeType.ALL)
    private List<TravelPlan> travelPlans = new ArrayList<>();

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AccountFlight> accountFlights = new ArrayList<>();

    @Column(nullable = false)
    private int levelExp = 0;

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL)
    private List<SavedPlace> savedPlaces;

    public TravelLevel getLevel() {
        return TravelLevel.findByExp(this.levelExp);
    }

    public void addExp(int exp) {
        this.levelExp += exp;
    }

    // 구글 로그인 사용자 생성자
    public Account(String email, String nickname, String provider, String providerId) {
        this.email = email;
        this.nickname = nickname;
        this.name = nickname;
        this.provider = provider;
        this.providerId = providerId;
        this.password = "SOCIAL_LOGIN";

        // 기본값 설정 (nullable 필드 제외)
        this.role = "USER";
        this.agreeTerms = true; // 소셜은 기본 동의 처리
        this.agreeMarketing = false;
        this.levelExp = 0;
    }
}
