package com.pranit.github.helper;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;

public final class Generate {
    private static final SecureRandom RANDOM = new SecureRandom();

    private Generate() {
    }

    public static String generateEventId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public static String generateJti() {
        return generateToken();
    }

    private static String generateToken() {
        byte[] randomBytes = new byte[64];
        RANDOM.nextBytes(randomBytes);
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(randomBytes);
    }
}
