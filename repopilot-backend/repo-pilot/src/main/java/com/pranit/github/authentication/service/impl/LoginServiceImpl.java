package com.pranit.github.authentication.service.impl;

import com.pranit.github.authentication.dto.GithubOauth2Response;
import com.pranit.github.authentication.dto.UserResponse;
import com.pranit.github.authentication.helper.CurrentUser;
import com.pranit.github.authentication.service.LoginService;
import com.pranit.github.entities.model.UserDetail;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public final class LoginServiceImpl implements LoginService {

    @Override
    public GithubOauth2Response githubOauth2Url() {
        return GithubOauth2Response.builder()
                .url(Map.of("url", "/oauth2/authorization/github"))
                .build();
    }

    @Override
    public UserResponse getCurrentUser() {
        final UserDetail principal = CurrentUser.getUserDetail();
        return UserResponse.builder()
                .userId(principal.userId())
                .githubId(principal.githubId())
                .githubUsername(principal.githubUsername())
                .displayName(principal.displayName())
                .avatarUrl(principal.avatarUrl())
                .build();
    }
}
