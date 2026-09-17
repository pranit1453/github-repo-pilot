package com.pranit.github.authentication.oauth2.service;

import com.pranit.github.authentication.oauth2.model.GithubUserInfo;
import com.pranit.github.entities.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class GithubOAuth2AuthenticationService {

    private final GitHubUserInfoExtractor gitHubUserInfoExtractor;
    private final GithubOAuth2UserProvisioningService provisioningService;

    @Transactional
    public User authenticate(final OAuth2User oauth2User, final String accessToken, final String tokenScopes) {
        log.debug("GitHub OAuth2 authentication started");
        final GithubUserInfo userInfo = gitHubUserInfoExtractor.extractUserInfo(oauth2User, accessToken, tokenScopes);
        final User user = provisioningService.provisionUser(userInfo);
        log.info("GitHub OAuth2 authentication successful. userId: {}", user.getUserId());
        return user;
    }
}