package com.backfunctionimpl.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;

import java.util.Optional;

@Configuration
public class AuditConfig {

    @Bean
    public AuditorAware<String> auditorProvider() {
        // 여기서 현재 로그인 사용자 정보를 반환하면 가장 좋지만,
        // 지금은 간단하게 "system" 이라는 더미 사용자로 설정
        return () -> Optional.of("system");
    }
}
