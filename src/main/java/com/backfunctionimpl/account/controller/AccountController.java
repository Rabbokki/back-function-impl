package com.backfunctionimpl.account.controller;

import com.backfunctionimpl.account.dto.AccountRegisterRequestDto;
import com.backfunctionimpl.account.dto.LoginRequestDto;
import com.backfunctionimpl.account.service.AccountService;
import com.backfunctionimpl.global.security.jwt.dto.TokenDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    //회원가입
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody AccountRegisterRequestDto request) {
        accountService.register(request);
        return ResponseEntity.ok("회원가입 성공!");
    }

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<TokenDto> login(@RequestBody LoginRequestDto request) {
        TokenDto tokenDto = accountService.login(request);
        return ResponseEntity.ok(tokenDto);
    }
}
