package com.nxt.user_service.repo;

import com.nxt.user_service.auth.AuthProvider;
import com.nxt.user_service.entity.UserIdentity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserIdentityRepository extends JpaRepository<UserIdentity, Long> {
    Optional<UserIdentity> findByAuthProviderAndExternalId(AuthProvider authProvider, String externalId);
}