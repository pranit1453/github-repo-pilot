package com.pranit.github.authentication.service.impl;

import com.pranit.github.authentication.exception.UserNotExistsException;
import com.pranit.github.authentication.repository.UserRepository;
import com.pranit.github.authentication.service.UserDetailService;
import com.pranit.github.authorization.repository.UserRoleRepository;
import com.pranit.github.entities.entity.User;
import com.pranit.github.entities.model.UserDetail;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailServiceImpl implements UserDetailService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public @NullMarked UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        final User user = userRepository.findByGithubUsername(username)
                .orElseThrow(() -> {
                    log.warn("Username {} not found", username);
                    return new UsernameNotFoundException("Invalid username or password");
                });

        final Set<GrantedAuthority> authorities = fetchAuthorities(user.getUserId());

        return UserDetail.builder()
                .userId(user.getUserId())
                .githubId(user.getGithubId())
                .githubUsername(user.getGithubUsername())
                .displayName(user.getDisplayName())
                .avatarUrl(user.getAvatarUrl())
                .authorities(authorities)
                .attributes(Map.of())
                .build();
    }

    private Set<GrantedAuthority> fetchAuthorities(final UUID userId) {
        return userRoleRepository.findAuthoritiesByUserId(userId)
                .stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public UserDetail loadUserByUserId(final UUID userId) {
        final User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> {
                    log.warn("UserId {} not found", userId);
                    return new UserNotExistsException("Invalid username or passwords");
                });

        final Set<GrantedAuthority> authorities = fetchAuthorities(user.getUserId());

        return UserDetail.builder()
                .userId(user.getUserId())
                .githubId(user.getGithubId())
                .githubUsername(user.getGithubUsername())
                .displayName(user.getDisplayName())
                .avatarUrl(user.getAvatarUrl())
                .authorities(authorities)
                .attributes(Map.of())
                .build();
    }
}
