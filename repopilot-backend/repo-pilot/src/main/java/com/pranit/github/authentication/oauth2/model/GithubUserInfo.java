package com.pranit.github.authentication.oauth2.model;

import lombok.Builder;

@Builder
public record GithubUserInfo(
        Long githubId,
        String githubUsername,
        String displayName,
        String avatarUrl,
        String accessToken,
        String tokenScopes
) {
}