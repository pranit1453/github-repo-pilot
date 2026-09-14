package com.pranit.github.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "security.crypto")
public record CryptoProperties(
        String tokenEncryptorPassword,
        String tokenEncryptorSalt
) {
}