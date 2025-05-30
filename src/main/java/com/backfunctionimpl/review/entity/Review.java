package com.backfunctionimpl.review.entity;

import com.backfunctionimpl.account.entity.Account;
import com.backfunctionimpl.account.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "review")
public class Review extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "place_id", nullable = false)
    private String placeId;

    @Column(name = "place_name", nullable = false, length = 300)
    private String placeName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_account_id")
    private Account account;

    @Column(nullable = false)
    private int rating;

    @Column(length = 500)
    private String nickname;

    @Column(length = 1000)
    private String content;

    @Transient
    private Long postId;

    public Review(String placeId, String placeName, Account account, int rating, String nickname, String content) {
        this.placeId = placeId;
        this.placeName = placeName;
        this.account = account;
        this.rating = rating;
        this.nickname = nickname;
        this.content = content;
    }
}

