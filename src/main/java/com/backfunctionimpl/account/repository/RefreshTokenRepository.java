package com.backfunctionimpl.account.repository;

import com.backfunctionimpl.account.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken,Long> {
    Optional<RefreshToken> findByAccountEmail(String email);
    void deleteByAccountEmail(String email);
}
