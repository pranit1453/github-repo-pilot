package com.pranit.github.helper;

import com.pranit.github.authentication.exception.UnauthorizedException;
import com.pranit.github.entities.model.UserDetail;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

@Slf4j
public final class SecurityContext {

    private SecurityContext() {
    }

    public static UUID getCurrentUserId() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null ||
                !authentication.isAuthenticated() ||
                authentication instanceof AnonymousAuthenticationToken) {
            log.warn("User is not authenticated: {}", authentication);
            throw new UnauthorizedException("User not authenticated");
        }
        final Object principal = authentication.getPrincipal();
        if (!(principal instanceof UserDetail userDetail)) {
            log.warn("Invalid authentication principal: {}", principal);
            throw new UnauthorizedException("Invalid authentication principal");
        }
        return userDetail.userId();
    }
}
