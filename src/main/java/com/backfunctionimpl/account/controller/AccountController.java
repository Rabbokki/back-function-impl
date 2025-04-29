package com.backfunctionimpl.account.controller;

import com.backfunctionimpl.account.dto.AccountRegisterRequestDto;
import com.backfunctionimpl.account.dto.AccountResponseDto;
import com.backfunctionimpl.account.dto.AccountUpdateRequestDto;
import com.backfunctionimpl.account.dto.LoginRequestDto;
import com.backfunctionimpl.account.service.AccountService;
import com.backfunctionimpl.global.security.jwt.dto.TokenDto;
import com.backfunctionimpl.global.security.jwt.util.JwtUtil;
import com.backfunctionimpl.global.security.user.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;
    private final JwtUtil jwtUtil;

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

    //로그아웃
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader("Access_Token") String accessToken) {
        String email = jwtUtil.getEmailFromToken(accessToken);
        accountService.logout(email);
        return ResponseEntity.ok("로그아웃 성공");
    }

    //회원 정보 조회 (로그인한 사용자만 가능)

    @GetMapping("/mypage")
    public ResponseEntity<AccountResponseDto> getMyInfo(
            @AuthenticationPrincipal UserDetailsImpl userDetails){
        return ResponseEntity.ok(accountService.getMyInfo(userDetails));
    }

    //회원 정보 수정 (닉네임, 비밀번호)
    @PutMapping("/mypage")
    public ResponseEntity<AccountResponseDto> updateMyInfo(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody AccountUpdateRequestDto updateDto) {
        return ResponseEntity.ok(accountService.updateMyInfo(userDetails, updateDto));
    }

}
