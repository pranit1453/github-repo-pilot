package com.pranit.github.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "security.keys")
public record KeysProperties(
        String privateKeyPath,
        String publicKeyPath
) {
}
