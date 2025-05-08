package com.backfunctionimpl.account.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccountPasswordChangeRequestDto {
    private String currentPassword;
    private String newPassword;
}