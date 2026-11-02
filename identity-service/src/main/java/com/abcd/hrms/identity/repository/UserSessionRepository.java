package com.abcd.hrms.identity.repository;

import com.abcd.hrms.identity.entity.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, UUID> {

    Optional<UserSession> findByRefreshTokenHashAndIsRevokedFalse(String refreshTokenHash);
}
