package com.pranit.github.entities.model;

import lombok.Builder;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

@Builder
public record UserDetail(
        @NonNull UUID userId,
        @NonNull Long githubId,
        @NonNull String githubUsername,
        @NonNull String displayName,
        @Nullable String avatarUrl,
        @NonNull Collection<? extends GrantedAuthority> authorities,
        @NonNull Map<String, Object> attributes
) implements UserDetails, OAuth2User {

    @Override
    public @NonNull Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public @NonNull Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public @Nullable String getPassword() {
        return null;
    }

    @Override
    public @NonNull String getUsername() {
        return githubUsername;
    }

    @Override
    public @NonNull String getName() {
        return githubUsername;
    }
}