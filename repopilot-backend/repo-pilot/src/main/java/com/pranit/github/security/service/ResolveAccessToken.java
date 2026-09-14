package com.pranit.github.security.service;

import jakarta.servlet.http.HttpServletRequest;

@FunctionalInterface
public interface ResolveAccessToken {

    String getAccessTokenFromRequest(HttpServletRequest request);
}
