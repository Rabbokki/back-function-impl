package com.backfunctionimpl.account.controller;

import com.backfunctionimpl.account.dto.AccountRegisterRequestDto;
import com.backfunctionimpl.account.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody AccountRegisterRequestDto request) {
        accountService.register(request);
        return ResponseEntity.ok("회원가입 성공!");
    }
}
