package com.backfunctionimpl.account.controller;

import com.backfunctionimpl.account.dto.*;
import com.backfunctionimpl.account.service.AccountService;
import com.backfunctionimpl.global.security.jwt.dto.TokenDto;
import com.backfunctionimpl.global.security.jwt.util.JwtUtil;
import com.backfunctionimpl.global.security.user.UserDetailsImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;
    private final JwtUtil jwtUtil;

    //회원가입
    @PostMapping("/register")
    public ResponseEntity<String> register(
            @RequestPart("request") @Valid AccountRegisterRequestDto request,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage) {

        accountService.register(request, profileImage);
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
            @RequestPart("request") AccountUpdateRequestDto updateDto,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage
    ) {
        return ResponseEntity.ok(accountService.updateMyInfo(userDetails, updateDto, profileImage));
    }

    @PutMapping("/password")
    public ResponseEntity<String> changePassword(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody AccountPasswordChangeRequestDto dto
    ) {
        accountService.changePassword(userDetails, dto);
        return ResponseEntity.ok("비밀번호가 성공적으로 변경되었습니다.");
    }

    @DeleteMapping
    public ResponseEntity<String> deleteAccount(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        accountService.deleteAccount(userDetails);
        return ResponseEntity.ok("계정이 성공적으로 삭제되었습니다.");
    }



}