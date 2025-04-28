package com.backfunctionimpl.account.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class AccountRegisterRequest {
    private String email;
    private String password;
    private String nickname;
    private LocalDate birthday;
}
