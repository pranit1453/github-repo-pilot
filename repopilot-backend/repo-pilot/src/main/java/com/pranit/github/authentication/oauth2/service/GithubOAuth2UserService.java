package com.pranit.github.authentication.oauth2.service;

import com.pranit.github.authorization.repository.UserRoleRepository;
import com.pranit.github.constant.GithubScope;
import com.pranit.github.entities.entity.User;
import com.pranit.github.entities.model.UserDetail;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GithubOAuth2UserService extends DefaultOAuth2UserService {

    private final GithubOAuth2AuthenticationService authenticationService;
    private final UserRoleRepository userRoleRepository;

    @Override
    public @NullMarked OAuth2User loadUser(final OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        final OAuth2User githubUser = super.loadUser(userRequest);
        final String accessToken = userRequest.getAccessToken().getTokenValue();
        final String tokenScopes = Optional.of(userRequest.getAccessToken().getScopes())
                .map(scopes -> String.join(",", scopes))
                .orElse(GithubScope.SCOPE);
        final User user = authenticationService.authenticate(githubUser, accessToken, tokenScopes);
        final Set<GrantedAuthority> authorities = fetchAuthorities(user.getUserId());
        return UserDetail.builder()
                .userId(user.getUserId())
                .githubId(user.getGithubId())
                .githubUsername(user.getGithubUsername())
                .displayName(user.getDisplayName())
                .avatarUrl(user.getAvatarUrl())
                .authorities(authorities)
                .attributes(githubUser.getAttributes())
                .build();
    }

    private Set<GrantedAuthority> fetchAuthorities(final UUID userId) {
        return userRoleRepository.findAuthoritiesByUserId(userId)
                .stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toUnmodifiableSet());
    }
}