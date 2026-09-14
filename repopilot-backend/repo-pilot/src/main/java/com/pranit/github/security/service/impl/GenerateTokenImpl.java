package com.pranit.github.security.service.impl;

import com.pranit.github.constant.JwtClaim;
import com.pranit.github.entities.model.UserDetail;
import com.pranit.github.properties.KeysProperties;
import com.pranit.github.properties.TokenProperties;
import com.pranit.github.security.helper.LoadKey;
import com.pranit.github.security.service.GenerateToken;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.security.PrivateKey;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public final class GenerateTokenImpl implements GenerateToken {

    private final TokenProperties properties;
    private final PrivateKey privateKey;

    public GenerateTokenImpl(TokenProperties properties, KeysProperties keysPath) {
        this.properties = properties;
        this.privateKey = LoadKey.loadPrivateKey(keysPath.privateKeyPath());
    }

    @Override
    public String generateAccessToken(final UserDetail userDetail) {
        final UUID userId = userDetail.userId();
        final String username = userDetail.githubUsername();
        final Instant now = Instant.now();
        final Instant expiry = now.plusSeconds(properties.accessToken().expiration());
        final Map<Boolean, Set<String>> grouped = userDetail.authorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(Objects::nonNull)
                .collect(Collectors.partitioningBy(
                        a -> a.startsWith("ROLE_"),
                        Collectors.toUnmodifiableSet()));
        final Set<String> roles = grouped.get(true);
        return Jwts.builder()
                .id(userId.toString())
                .subject(username)
                .issuer(properties.token().issuer())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .claim(JwtClaim.ROLES, roles)
                .claim(JwtClaim.TOKEN_TYPE, JwtClaim.ACCESS)
                .signWith(this.privateKey)
                .compact();
    }

    @Override
    public String generateRefreshToken(final UserDetail userDetail, final String jti) {
        final UUID userId = userDetail.userId();
        final Instant now = Instant.now();
        final Instant expiry = now.plusSeconds(properties.refreshToken().expiration());
        return Jwts.builder()
                .id(jti)
                .subject(userId.toString())
                .issuer(properties.token().issuer())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .claim(JwtClaim.TOKEN_TYPE, JwtClaim.REFRESH)
                .signWith(this.privateKey)
                .compact();
    }
}
