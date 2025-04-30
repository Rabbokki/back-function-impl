package com.backfunctionimpl.account.service;

import com.backfunctionimpl.account.dto.AccountRegisterRequestDto;
import com.backfunctionimpl.account.dto.AccountResponseDto;
import com.backfunctionimpl.account.dto.AccountUpdateRequestDto;
import com.backfunctionimpl.account.dto.LoginRequestDto;
import com.backfunctionimpl.account.entity.Account;
import com.backfunctionimpl.account.entity.RefreshToken;
import com.backfunctionimpl.account.entity.TravelLevel;
import com.backfunctionimpl.account.repository.AccountRepository;
import com.backfunctionimpl.account.repository.RefreshTokenRepository;
import com.backfunctionimpl.global.security.jwt.dto.TokenDto;
import com.backfunctionimpl.global.security.jwt.util.JwtUtil;
import com.backfunctionimpl.global.security.user.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        // 필수 약관 동의 확인
        if (!request.isAgreeTerms()) {
            throw new IllegalArgumentException("이용약관 및 개인정보 처리방침에 동의해야 가입할 수 있습니다.");
        }

        // Account 객체 생성
        Account account = new Account();
        account.setEmail(request.getEmail());
        account.setName(request.getName());
        account.setPassword(passwordEncoder.encode(request.getPassword()));
        account.setNickname(request.getNickname());
        account.setBirthday(request.getBirthday());
        account.setImgUrl(null); // 필요 시 프로필 이미지 기본값 지정
        account.setProvider(null);
        account.setProviderId(null);

        // TravelLevel 기본값 세팅
        account.setLevel(TravelLevel.BEGINNER);
        account.setLevelExp(0);

        // 약관 동의 여부 저장
        account.setAgreeTerms(request.isAgreeTerms());         // 무조건 true여야 통과
        account.setAgreeMarketing(request.isAgreeMarketing()); // 선택 사항

        // DB에 저장
        accountRepository.save(account);
    }


    //로그인
    @Transactional
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

        // 4. RefreshToken DB 저장
        RefreshToken refreshToken = RefreshToken.builder()
                .accountEmail(account.getEmail())
                .refreshToken(tokenDto.getRefreshToken())
                .build();
        refreshTokenRepository.save(refreshToken);


        // 5. 발급된 토큰 반환
        return tokenDto;
    }

    //로그아웃
    public void logout(String email) {
        refreshTokenRepository.deleteByAccountEmail(email);
    }


    //회원정보 조회
    public AccountResponseDto getMyInfo(@AuthenticationPrincipal UserDetailsImpl userDetails
                                        ) {
        String email = userDetails.getAccount().getEmail();
        Account account = accountRepository.findByEmail(email).orElseThrow(()->
                new RuntimeException("해당 이메일을 찾을 수 없습니다."));

        return new AccountResponseDto(account);
    }

    //회원정보 수정

    public AccountResponseDto updateMyInfo(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            AccountUpdateRequestDto updateDto) {

        String email = userDetails.getAccount().getEmail();

        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("해당 이메일을 찾을 수 없습니다."));

        if (updateDto.getNickname() != null) {
            account.setNickname(updateDto.getNickname());
        }

        if (updateDto.getPassword() != null) {
            account.setPassword(passwordEncoder.encode(updateDto.getPassword()));
        }

        return new AccountResponseDto(account);
    }


}
