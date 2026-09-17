package com.pranit.github.authentication.oauth2.service;

import com.pranit.github.authentication.oauth2.model.GithubUserInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
public class GitHubUserInfoExtractor {

    public GithubUserInfo extractUserInfo(final OAuth2User user, final String accessToken, final String tokenScopes) {
        final Long githubId = Optional.ofNullable(user.getAttribute("id"))
                .map(value -> ((Number) value).longValue())
                .orElseThrow(() -> new OAuth2AuthenticationException("GitHub user id not found"));
        final String githubUsername = Optional.ofNullable(user.getAttribute("login"))
                .map(Object::toString)
                .filter(username -> !username.isBlank())
                .orElseThrow(() -> new OAuth2AuthenticationException("GitHub username not found"));
        final String displayName = Optional.ofNullable(user.getAttribute("name"))
                .map(Object::toString)
                .filter(name -> !name.isBlank())
                .orElse(githubUsername);
        final String avatarUrl = Optional.ofNullable(user.getAttribute("avatar_url"))
                .map(Object::toString)
                .orElse(null);
        log.debug("GitHub user extracted successfully. githubId: {}, username: {}", githubId, githubUsername);
        return GithubUserInfo.builder()
                .githubId(githubId)
                .githubUsername(githubUsername)
                .displayName(displayName)
                .avatarUrl(avatarUrl)
                .accessToken(accessToken)
                .tokenScopes(tokenScopes)
                .build();
    }
}