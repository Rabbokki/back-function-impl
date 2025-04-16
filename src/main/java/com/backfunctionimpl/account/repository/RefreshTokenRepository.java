package com.backfunctionimpl.account.repository;

import com.backfunctionimpl.account.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken,Long> {
    Optional<RefreshToken> findByAccountEmail(String email);
}
