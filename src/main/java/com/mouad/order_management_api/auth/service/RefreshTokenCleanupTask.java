package com.mouad.order_management_api.auth.service;

import com.mouad.order_management_api.auth.repository.RefreshTokenRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Component
public class RefreshTokenCleanupTask {
    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshTokenCleanupTask(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void purgeExpiredTokens() {
        refreshTokenRepository.deleteByExpiresAtBefore(Instant.now());
    }
}