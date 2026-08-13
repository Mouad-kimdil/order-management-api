package com.mouad.order_management_api.auth.repository;

import com.mouad.order_management_api.auth.model.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    Optional<RefreshToken> findByTokenHash(String tokenHash);

    @Modifying
    @Transactional(propagation = Propagation.REQUIRES_NEW)

    @Query("UPDATE RefreshToken r SET r.revoked = true WHERE r.familyId = :familyId")
    void revokeAllByFamilyId(@Param("familyId") UUID familyId);

    void deleteByExpiresAtBefore(Instant now);
}
