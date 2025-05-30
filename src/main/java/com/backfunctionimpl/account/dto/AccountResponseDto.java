package com.backfunctionimpl.account.dto;

import com.backfunctionimpl.account.entity.Account;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor

//회원정보 조회용 dto
public class AccountResponseDto {
    private Long id;
    private String email;
    private String name;
    private String nickname;
    public String role;
    private String imgUrl;
    private String bio;
    private String gender;
    private String birthday;
    private String level;
    private int levelExp;
    private LocalDateTime createdAt;

    // Account 객체로부터 변환
    public AccountResponseDto(Account account) {
        this.id = account.getId();
        this.email = account.getEmail();
        this.name = account.getName();
        this.nickname = account.getNickname();
        this.role = account.getRole();
        this.imgUrl = account.getImgUrl();
        this.bio = account.getBio();
        this.gender = account.getGender();
        this.birthday= String.valueOf(account.getBirthday());
        this.level = account.getLevel().name();
        this.levelExp = account.getLevelExp();
        this.createdAt = account.getCreatedAt();
    }
}
