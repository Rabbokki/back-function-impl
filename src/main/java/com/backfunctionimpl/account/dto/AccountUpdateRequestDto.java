package com.backfunctionimpl.account.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor

//회원정보 수정 요청 dto
public class AccountUpdateRequestDto {
    private String nickname;
    private String password;
    private String bio;
    private String gender;
    private String birthday;
    private String imgUrl;
}
