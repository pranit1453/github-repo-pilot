package com.pranit.github.controller;

import com.pranit.github.authentication.dto.GithubOauth2Response;
import com.pranit.github.authentication.dto.TokenResponse;
import com.pranit.github.authentication.dto.UserResponse;
import com.pranit.github.authentication.exception.UnauthorizedException;
import com.pranit.github.authentication.service.LoginService;
import com.pranit.github.authentication.service.LogoutService;
import com.pranit.github.authentication.service.TokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Validated
@Tag(
        name = "Authentication",
        description = "Endpoints for GitHub OAuth2 login, token management, session validation, and user logout."
)
public class LoginController {

    private final LoginService loginService;
    private final TokenService tokenService;
    private final LogoutService logoutService;

    @Operation(
            summary = "Start GitHub OAuth2 login",
            description = "Generates the GitHub OAuth2 authorization URL and redirects the client to GitHub to begin the authentication flow."
    )
    @GetMapping(value = "/login", version = "v1")
    public ResponseEntity<Void> loginUrl() {
        final GithubOauth2Response response = loginService.githubOauth2Url();
        final String authorizationUrl = response.url().get("url");
        log.info("Redirecting to GitHub OAuth2 authorization URL: {}", authorizationUrl);
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(authorizationUrl))
                .build();
    }

    @Operation(
            summary = "Get current authenticated user",
            description = "Returns the profile information of the user associated with the current authenticated session or access token."
    )
    @GetMapping(value = "/me", version = "v1")
    public ResponseEntity<UserResponse> me() {
        return ResponseEntity.status(HttpStatus.OK).body(loginService.getCurrentUser());
    }

    @Operation(
            summary = "Refresh access token",
            description = "Validates the refresh token from the request and issues a new access token and refresh token for continued authentication."
    )
    @PostMapping(value = "/refresh", version = "v1")
    public ResponseEntity<TokenResponse> refreshToken(final HttpServletRequest request, final HttpServletResponse response) {
        final String refreshToken = tokenService.readRefreshTokenFromRequest(request)
                .orElseThrow(() -> {
                    log.warn("No refresh token found");
                    return new UnauthorizedException("No refresh token found");
                });
        final TokenResponse tokenResponse = tokenService.generateNewRefreshToken(refreshToken, response);
        return ResponseEntity.status(HttpStatus.OK)
                .body(tokenResponse);
    }

    @Operation(
            summary = "Logout authenticated user",
            description = "Revokes the user's access and refresh tokens and clears authentication cookies to terminate the current authenticated session."
    )
    @PostMapping(value = "/logout", version = "v1")
    public ResponseEntity<String> logout(@Valid HttpServletRequest request, @Valid HttpServletResponse response) {
        logoutService.readRefreshTokenFromRequest(request)
                .ifPresent(logoutService::revokedRefreshToken);
        logoutService.readAccessTokenFromRequest(request)
                .ifPresent(logoutService::revokedAccessToken);
        logoutService.clearResponse(response);
        log.info("User logged out successfully");
        return ResponseEntity.ok("Logout successful");
    }
}
