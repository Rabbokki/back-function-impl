package com.backfunctionimpl.account.oauth2.google;

import com.backfunctionimpl.account.entity.Account;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

@Getter
@Slf4j
public class CustomUserDetails implements UserDetails, OAuth2User {
    private final Account account;
    private Map<String, Object> attributes;

    //일반 로그인
    public CustomUserDetails(Account account){
        this.account = account;
    }

    //구글 로그인
    public CustomUserDetails(Account account, Map<String, Object> attributes){
        this.account = account;
        this.attributes = attributes;
        log.info("CustomUserDetails attributes: {}", attributes); // 디버깅 로그 추가
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")); // 기본 권한 설정
    }

    @Override
    public String getPassword() {
        return account.getPassword(); // Google 계정은 null일 수 있음
    }

    @Override
    public String getUsername() {
        return account.getEmail(); // 이메일을 사용자 이름으로 사용
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public String getName() {
        return account.getNickname(); // OAuth2User의 이름
    }
}
