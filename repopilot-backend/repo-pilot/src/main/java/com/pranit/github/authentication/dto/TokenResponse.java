package com.pranit.github.authentication.dto;

import lombok.Builder;

@Builder
public record TokenResponse(
        String message
) {
}
