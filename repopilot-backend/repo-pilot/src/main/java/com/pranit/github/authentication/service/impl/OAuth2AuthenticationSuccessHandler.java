package com.pranit.github.authentication.service.impl;

import com.pranit.github.authentication.repository.RefreshTokenRepository;
import com.pranit.github.authentication.repository.UserRepository;
import com.pranit.github.entities.entity.RefreshToken;
import com.pranit.github.entities.entity.User;
import com.pranit.github.entities.model.UserDetail;
import com.pranit.github.helper.Generate;
import com.pranit.github.properties.Oauth2UrlProperties;
import com.pranit.github.properties.TokenProperties;
import com.pranit.github.security.service.CookieService;
import com.pranit.github.security.service.GenerateToken;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
@NullMarked
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenProperties properties;
    private final GenerateToken generateToken;
    private final CookieService cookieService;
    private final Oauth2UrlProperties oauth2UrlProperties;

    @Override
    @Transactional
    public void onAuthenticationSuccess(final HttpServletRequest request, final HttpServletResponse response, final Authentication authentication) throws IOException {
        log.info("GitHub OAuth2 authentication success");
        if (!(authentication.getPrincipal() instanceof UserDetail userDetail)) {
            log.error("Unexpected authentication principal: {}", Objects.requireNonNull(authentication.getPrincipal()).getClass().getName());
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Invalid authentication principal");
            return;
        }
        final UUID userId = userDetail.userId();
        final String jti = Generate.generateJti();
        final Instant now = Instant.now();
        final User user = userRepository.getReferenceById(userId);

        final RefreshToken rt = RefreshToken.builder()
                .jti(jti)
                .user(user)
                .expiresAt(now.plus(Duration.ofSeconds(properties.refreshToken().expiration())))
                .revoked(false)
                .build();
        refreshTokenRepository.save(rt);
        final String accessToken = generateToken.generateAccessToken(userDetail);
        final String refreshToken = generateToken.generateRefreshToken(userDetail, jti);
        final Duration accessTokenTtl = Duration.ofSeconds(properties.accessToken().expiration());
        final Duration refreshTokenTtl = Duration.ofSeconds(properties.refreshToken().expiration());
        cookieService.attachAccessTokenCookie(response, accessToken, accessTokenTtl);
        cookieService.attachRefreshTokenCookie(response, refreshToken, refreshTokenTtl);
        cookieService.addNoStoreHeaders(response);
        log.info("OAuth2 login completed successfully for userId: {}. Attached HTTP-only accessToken and refreshToken cookies.", userId);
        final String redirectUrl = oauth2UrlProperties.success();
        log.info("Redirecting to frontend success URL: {}", redirectUrl);
        response.sendRedirect(redirectUrl);
    }
}
