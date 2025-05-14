package com.backfunctionimpl.account.service;

import com.backfunctionimpl.account.dto.*;
import com.backfunctionimpl.account.entity.Account;
import com.backfunctionimpl.account.entity.RefreshToken;
import com.backfunctionimpl.account.entity.Role;
import com.backfunctionimpl.account.entity.TravelLevel;
import com.backfunctionimpl.account.repository.AccountRepository;
import com.backfunctionimpl.account.repository.RefreshTokenRepository;
import com.backfunctionimpl.global.security.jwt.dto.TokenDto;
import com.backfunctionimpl.global.security.jwt.util.JwtUtil;
import com.backfunctionimpl.global.security.user.UserDetailsImpl;
import com.backfunctionimpl.s3.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;
    private final S3Service s3Service;


    //  회원가입
    public void register(AccountRegisterRequestDto request, MultipartFile profileImage) {
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
        account.setRole("USER");
        account.setName(request.getName());
        account.setRole("USER");
        account.setPassword(passwordEncoder.encode(request.getPassword()));
        account.setNickname(request.getNickname());
        account.setBirthday(request.getBirthday());
        account.setBio(request.getBio());
        account.setGender(request.getGender());
        account.setRole(Role.USER);

       //  프로필 이미지 업로드 처리 (예: S3 업로드 or 로컬 저장)
        if (profileImage != null && !profileImage.isEmpty()) {
            String imageUrl = s3Service.uploadFile(profileImage);// <- 여기서 S3에 저장됨
            account.setImgUrl(imageUrl);
        } else {
            account.setImgUrl("/images/default-profile.png"); // 기본 이미지
        }
        account.setProvider(null);
        account.setProviderId(null);

        // TravelLevel 기본값 세팅
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

        System.out.println("🚀 로그인 요청됨: " + request.getEmail());
        // 1. 이메일로 사용자 찾기
        Account account = accountRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 이메일입니다."));


        // 2. 비밀번호 매칭
        if (!passwordEncoder.matches(request.getPassword(), account.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // 3. JWT 토큰 생성
        TokenDto tokenDto = jwtUtil.createAllToken(account.getEmail(), account.getRole());

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
    public AccountResponseDto getMyInfo(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        if (userDetails == null) {
            throw new RuntimeException("인증된 사용자 정보가 없습니다. (로그인 필요)");
        }

        String email = userDetails.getAccount().getEmail();

        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("해당 이메일을 찾을 수 없습니다."));

        return new AccountResponseDto(account);
    }


    //회원정보 수정

    public AccountResponseDto updateMyInfo(
            UserDetailsImpl userDetails,
            AccountUpdateRequestDto updateDto,
            MultipartFile profileImage // ✅ 이미지도 함께 받음
    ) {
        String email = userDetails.getAccount().getEmail();

        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("해당 이메일을 찾을 수 없습니다."));

        // 닉네임 수정
        if (updateDto.getNickname() != null) {
            account.setNickname(updateDto.getNickname());
        }

        // 비밀번호 수정
        if (updateDto.getPassword() != null) {
            account.setPassword(passwordEncoder.encode(updateDto.getPassword()));
        }

        // 이미지 수정 /삭제
        if (profileImage != null && !profileImage.isEmpty()) {
            String imageUrl = s3Service.uploadFile(profileImage); // 업로드
            account.setImgUrl(imageUrl);
        } else if (updateDto.getImgUrl() != null && updateDto.getImgUrl().isEmpty()) {
            // 👉 프론트에서 이미지 삭제 요청 (imgUrl: '')
            account.setImgUrl("/images/default-profile.png"); // 기본 이미지 설정
        }

        // 자기소개, 성별, 출생년도 등
        if (updateDto.getBio() != null) {
            account.setBio(updateDto.getBio());
        }

        if (updateDto.getGender() != null) {
            account.setGender(updateDto.getGender());
        }

        if (updateDto.getBirthday() != null && !updateDto.getBirthday().isEmpty()) {
            try {
                LocalDate birthDate = LocalDate.parse(updateDto.getBirthday());
                account.setBirthday(birthDate);
            } catch (Exception e) {
                throw new IllegalArgumentException("올바른 생년월일 형식이 아닙니다. (예: 1990-01-01)");
            }
        }



        accountRepository.save(account);

        return new AccountResponseDto(account);
    }


    @Transactional
    public void changePassword(UserDetailsImpl userDetails, AccountPasswordChangeRequestDto dto) {
        try {
            if (userDetails == null || userDetails.getAccount() == null) {
                throw new IllegalStateException("인증 정보가 없습니다.");
            }

            Account account = accountRepository.findByEmail(userDetails.getAccount().getEmail())
                    .orElseThrow(() -> new RuntimeException("사용자 정보를 찾을 수 없습니다."));

            if (!passwordEncoder.matches(dto.getCurrentPassword(), account.getPassword())) {
                throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
            }

            account.setPassword(passwordEncoder.encode(dto.getNewPassword()));
            accountRepository.save(account);
        } catch (Exception e) {
            log.error("비밀번호 변경 실패", e);
            throw e; // 에러 메시지를 프론트로 보내기 위해 그대로 던짐
        }
    }

    //계정 삭제
    @Transactional
    public void deleteAccount(UserDetailsImpl userDetails) {
        Account account = accountRepository.findById(userDetails.getAccount().getId())
                .orElseThrow(() -> new IllegalArgumentException("계정을 찾을 수 없습니다."));

        // 진짜로 삭제
        accountRepository.delete(account);
    }

    @Transactional
    public void addExperience(UserDetailsImpl userDetails, int exp) {
        Account account = accountRepository.findById(userDetails.getAccount().getId())
                .orElseThrow(() -> new IllegalArgumentException("계정을 찾을 수 없습니다."));

        account.addExp(exp); //  경험치 증가 (Account 엔티티의 addExp 메서드)
        accountRepository.save(account);
    }




}
