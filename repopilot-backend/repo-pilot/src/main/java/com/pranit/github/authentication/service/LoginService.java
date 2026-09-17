package com.pranit.github.authentication.service;

import com.pranit.github.authentication.dto.GithubOauth2Response;
import com.pranit.github.authentication.dto.UserResponse;

public interface LoginService {

    GithubOauth2Response githubOauth2Url();

    UserResponse getCurrentUser();
}
