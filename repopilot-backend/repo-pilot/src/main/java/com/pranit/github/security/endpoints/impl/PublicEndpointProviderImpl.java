package com.pranit.github.security.endpoints.impl;

import com.pranit.github.security.endpoints.PublicEndpointProvider;
import org.springframework.stereotype.Service;

@Service
public class PublicEndpointProviderImpl implements PublicEndpointProvider {

    private static final String[] PUBLIC_ENDPOINTS = {
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/api/auth/login",
            "/api/auth/refresh",
            "/oauth2/**",
            "/login/oauth2/**"
    };

    @Override
    public String[] publicEndpoints() {
        return PUBLIC_ENDPOINTS;
    }
}
