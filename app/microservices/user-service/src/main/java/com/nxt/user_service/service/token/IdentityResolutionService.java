package com.nxt.user_service.service.token;

import com.nxt.user_service.entity.User;
import com.nxt.user_service.entity.UserIdentity;
import com.nxt.user_service.model.RegistrationType;
import com.nxt.user_service.model.VerifiedIdentity;
import com.nxt.user_service.repo.UserIdentityRepository;
import com.nxt.user_service.repo.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
public class IdentityResolutionService {

    private final UserIdentityRepository userIdentityRepository;
    private final UserRepository userRepository;

    @Autowired
    public IdentityResolutionService(UserIdentityRepository userIdentityRepository, UserRepository userRepository) {
        this.userIdentityRepository = userIdentityRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public User resolveOrCreateUser(VerifiedIdentity identity) {
        return userIdentityRepository
                .findByAuthProviderAndExternalId(identity.authProvider(), identity.externalId())
                .map(UserIdentity::getUser)
                .orElseGet(() -> {
                    User user = userRepository
                            .findByEmail(identity.email())
                            .orElseGet(() -> {
                                    User u = new User();
                                    u.setEmail(identity.email());
                                    u.setFirstName(identity.firstName());
                                    u.setLastName(identity.lastName());
                                    u.setRegistrationType(RegistrationType.OAUTH);
                                    return userRepository.save(u);
                            });

                    try {
                        UserIdentity identityEntity = new UserIdentity();
                        identityEntity.setUser(user);
                        identityEntity.setAuthProvider(identity.authProvider());
                        identityEntity.setExternalId(identity.externalId());
                        identityEntity.setEmail(identity.email());
                        userIdentityRepository.save(identityEntity);
                    } catch(DataIntegrityViolationException e) {
                        return userIdentityRepository
                                .findByAuthProviderAndExternalId(identity.authProvider(), identity.externalId())
                                .orElseThrow(() -> e)
                                .getUser();
                    }
                    return user;
                });
    }
}