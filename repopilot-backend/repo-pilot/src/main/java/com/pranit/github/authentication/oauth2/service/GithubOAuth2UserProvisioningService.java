package com.pranit.github.authentication.oauth2.service;

import com.pranit.github.authentication.exception.UserNotExistsException;
import com.pranit.github.authentication.oauth2.model.GithubUserInfo;
import com.pranit.github.authentication.repository.UserRepository;
import com.pranit.github.authorization.exception.RoleAlreadyAssignedException;
import com.pranit.github.authorization.exception.RoleNotFoundException;
import com.pranit.github.authorization.repository.RoleRepository;
import com.pranit.github.authorization.repository.UserRoleRepository;
import com.pranit.github.constant.UserMetadata;
import com.pranit.github.entities.constant.RoleStatus;
import com.pranit.github.entities.entity.Role;
import com.pranit.github.entities.entity.User;
import com.pranit.github.entities.entity.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class GithubOAuth2UserProvisioningService {

    private final UserRepository userRepository;
    private final TextEncryptor textEncryptor;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;

    @Transactional
    public User provisionUser(final GithubUserInfo userInfo) {
        return userRepository.findByGithubId(userInfo.githubId())
                .map(existingUser -> {
                    log.info("Existing OAuth2 user authenticated. userId: {}", existingUser.getUserId());
                    return existingUser;
                })
                .orElseGet(() -> createUser(userInfo));
    }

    private User createUser(final GithubUserInfo userInfo) {
        final Role defaultRole = roleRepository.findByRoleName(UserMetadata.USER_ROLE)
                .orElseThrow(() -> {
                    log.warn("Default role not found during OAuth2 provisioning. role: {}", UserMetadata.USER_ROLE);
                    return new RoleNotFoundException("Role not found");
                });
        User detail = User.builder()
                .githubId(userInfo.githubId())
                .githubUsername(userInfo.githubUsername())
                .displayName(userInfo.displayName())
                .avatarUrl(userInfo.avatarUrl())
                .accessToken(textEncryptor.encrypt(userInfo.accessToken()))
                .tokenScopes(userInfo.tokenScopes())
                .build();
        detail = userRepository.save(detail);
        addRoleToUser(detail.getUserId(), defaultRole);
        log.info("OAuth2 user provisioned successfully. userId: {}, assignedRole: {}", detail.getUserId(), UserMetadata.USER_ROLE);
        return detail;
    }

    private void addRoleToUser(final UUID userId, final Role role) {
        final User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> {
                    log.warn("No user credentials found for userId {}", userId);
                    return new UserNotExistsException("User not found");
                });
        if (userRoleRepository.existsByUser_UserIdAndRole_RoleId(userId, role.getRoleId())) {
            log.debug("Role '{}' already assigned to userId: {}", role.getRoleName(), userId);
            throw new RoleAlreadyAssignedException("Role already assigned");
        }
        final UserRole userRole = UserRole.builder()
                .user(user)
                .role(role)
                .status(RoleStatus.ACTIVE)
                .build();
        userRoleRepository.save(userRole);
        log.info("Assigned role '{}' to userId: {}", role.getRoleName(), userId);
    }
}