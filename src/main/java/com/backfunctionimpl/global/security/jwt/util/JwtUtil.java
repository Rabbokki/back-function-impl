package com.backfunctionimpl.global.security.jwt.util;

import com.backfunctionimpl.account.entity.RefreshToken;
import com.backfunctionimpl.account.repository.RefreshTokenRepository;
import com.backfunctionimpl.global.security.jwt.dto.TokenDto;
import com.backfunctionimpl.global.security.user.UserDetailsServiceImpl;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.Optional;

import io.jsonwebtoken.SignatureAlgorithm;


@Slf4j
@Component
@RequiredArgsConstructor
public class JwtUtil {
    private final UserDetailsServiceImpl userDetailsService;
    private final RefreshTokenRepository refreshTokenRepository;

    private static final long ACCESS_TIME = 24 * 60 * 60 * 1000L;
    private static final long REFRESH_TIME = 24 * 60 * 60 * 2000L;
    public static final String ACCESS_TOKEN = "Access_Token";
    public static final String REFRESH_TOKEN = "Refresh_Token";

    @Value("${jwt.secret.key}")
    private String secretKey;
    private Key key;
    private final SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

    @PostConstruct
    public void init() {
        byte[] bytes = Base64.getDecoder().decode(secretKey);
        key = Keys.hmacShaKeyFor(bytes);
    }

    // header 토큰을 가져오는 기능
    public String getHeaderToken(HttpServletRequest request, String headerName) {
        String token = request.getHeader(headerName);
        log.info("Header {} value: {}", headerName, token); // 디버깅용 로그 추가
        return token;
    }
    // 토큰 생성
    public TokenDto createAllToken(String email, String role){
        return new TokenDto(
                createToken(email, role, "Access"),
                createToken(email, role, "Refresh")
        );
    }

    public String createToken(String email, String role, String type) {

        Date date = new Date();

        long time = type.equals("Access") ? ACCESS_TIME : REFRESH_TIME;

        String token = Jwts.builder()
                .setSubject(email)
                .claim("role", role)
                .setExpiration(new Date(date.getTime() + time))
                .setIssuedAt(date)
                .signWith(key, signatureAlgorithm)
                .compact();
        log.info("Generated token: {}", token); // 토큰 생성 로그 추가
        return token;
    }
    // 토큰 검증
//    public Boolean tokenValidation(String token) {
//        try {
//            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
//            return true;
//        } catch (Exception ex) {
//            log.error(ex.getMessage());
//            return false;
//        }
//    }
    public boolean tokenValidation(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(Base64.getDecoder().decode(secretKey))  // secretKey를 byte[]로 디코딩
                    .build()
                    .parseClaimsJws(token);  // JWT 파싱
            log.info("Token validated successfully: {}", token);
            return true;
        } catch (Exception e) {
            log.error("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    public String getEmailFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(Base64.getDecoder().decode(secretKey))  // secretKey를 byte[]로 디코딩
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();  // 이메일을 가져옴
    }

    // refreshToken 토큰 검증
    public Boolean refreshTokenValidation(String token) {

        // 1차 토큰 검증
        if(!tokenValidation(token)) return false;

        // DB에 저장한 토큰 비교
        Optional<RefreshToken> refreshToken = refreshTokenRepository.findByAccountEmail(getEmailFromToken(token));

        return refreshToken.isPresent() && token.equals(refreshToken.get().getRefreshToken());
    }

    // 인증 객체 생성
    public Authentication createAuthentication(String email) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    // 토큰에서 email 가져오는 기능
//    public String getEmailFromToken(String token) {
//        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody().getSubject();
//    }


}