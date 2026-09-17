package com.pranit.github.authentication.dto;

import lombok.Builder;

import java.util.UUID;

@Builder
public record UserResponse(
        UUID userId,
        Long githubId,
        String githubUsername,
        String displayName,
        String avatarUrl
) {
}
