package com.backfunctionimpl.account.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor

//회원정보 수정 요청 dto
public class AccountUpdateRequestDto {
    private String nickname;
    private String password;
}
