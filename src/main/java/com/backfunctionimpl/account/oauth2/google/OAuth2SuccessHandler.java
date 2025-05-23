package com.backfunctionimpl.account.oauth2.google;

import com.backfunctionimpl.global.security.jwt.util.JwtUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;

    public OAuth2SuccessHandler(JwtUtil jwtUtil){
        this.jwtUtil = jwtUtil;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {

        // ✅ 1. 유저 정보 추출
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String email = userDetails.getUsername(); // 이메일 (아이디)

        // ✅ 2. 토큰 생성
        String accessToken = jwtUtil.createToken(email, "USER", "Access");
        String refreshToken = jwtUtil.createToken(email, "USER", "Refresh");

        log.info("✅ Generated Access Token: {}", accessToken);
        log.info("✅ Generated Refresh Token: {}", refreshToken);

        // ✅ 3. 리디렉션 경로: 배포 도메인으로 이동하면서 토큰 전달
        String redirectUrl = "https://travelling.p-e.kr/oauth/success"
                + "?accessToken=" + accessToken
                + "&refreshToken=" + refreshToken;

        log.info("✅ Redirecting to: {}", redirectUrl);

        // ✅ 4. 실제 리디렉션
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}
