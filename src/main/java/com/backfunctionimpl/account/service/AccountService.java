package com.backfunctionimpl.account.service;

import com.backfunctionimpl.account.dto.AccountRegisterRequestDto;
import com.backfunctionimpl.account.dto.LoginRequestDto;
import com.backfunctionimpl.account.entity.Account;
import com.backfunctionimpl.account.entity.RefreshToken;
import com.backfunctionimpl.account.entity.TravelLevel;
import com.backfunctionimpl.account.repository.AccountRepository;
import com.backfunctionimpl.account.repository.RefreshTokenRepository;
import com.backfunctionimpl.global.security.jwt.dto.TokenDto;
import com.backfunctionimpl.global.security.jwt.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;


    //  회원가입
    public void register(AccountRegisterRequestDto request) {
        // 이메일 중복 검사
        if (accountRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다.");
        }

        // Account 객체 생성
        Account account = new Account();
        account.setEmail(request.getEmail());
        account.setPassword(request.getPassword());
        account.setNickname(request.getNickname());
        account.setBirthday(request.getBirthday());

        // TravelLevel 기본값 세팅
        account.setLevel(TravelLevel.BEGINNER);
        account.setLevelExp(0);

        // DB에 저장
        accountRepository.save(account);
    }

    //로그인
    public TokenDto login(LoginRequestDto request) {
        // 1. 이메일로 사용자 찾기
        Account account = accountRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 이메일입니다."));

        // 2. 비밀번호 매칭
        if (!passwordEncoder.matches(request.getPassword(), account.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // 3. JWT 토큰 생성
        TokenDto tokenDto = jwtUtil.createAllToken(account.getEmail());

        // 4. (선택) RefreshToken DB 저장
        RefreshToken refreshToken = RefreshToken.builder()
                .accountEmail(account.getEmail())
                .refreshToken(tokenDto.getRefreshToken())
                .build();
        refreshTokenRepository.save(refreshToken);

        // 5. 발급된 토큰 반환
        return tokenDto;
    }

}
