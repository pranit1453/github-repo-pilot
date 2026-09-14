package com.pranit.github.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "oauth2")
public record Oauth2UrlProperties(
        String success,
        String failure
) {
}
