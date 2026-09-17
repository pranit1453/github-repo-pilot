package com.pranit.github.authentication.dto;

import lombok.Builder;

import java.util.Map;

@Builder
public record GithubOauth2Response(
        Map<String, String> url
) {
}
