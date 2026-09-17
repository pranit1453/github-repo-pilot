package com.pranit.github.authentication.service.impl;

import com.pranit.github.properties.Oauth2UrlProperties;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@NullMarked
@RequiredArgsConstructor
public class OAuth2AuthenticationFailureHandler implements AuthenticationFailureHandler {

    private final Oauth2UrlProperties properties;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        log.error("OAuth2 authentication failed. URI: {}, errorCode: {}", request.getRequestURI(), exception.getMessage(), exception);
        final String error = URLEncoder.encode("oauth2_authentication_failed", StandardCharsets.UTF_8);
        final String message = URLEncoder.encode(
                exception.getMessage() != null ? exception.getMessage() : "OAuth2 authentication failed",
                StandardCharsets.UTF_8
        );
        final String redirectUrl = properties.failure() + "?error=" + error + "&message=" + message;
        log.info("Redirecting to frontend failure URL: {}", redirectUrl);
        response.sendRedirect(redirectUrl);
    }
}
