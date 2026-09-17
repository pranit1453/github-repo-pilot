package com.pranit.github.security.config;

import com.pranit.github.properties.CryptoProperties;
import org.jspecify.annotations.NullMarked;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.encrypt.AesGcmBytesEncryptor;
import org.springframework.security.crypto.encrypt.TextEncryptor;

import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

@Configuration
public class CryptoConfig {

    @Bean
    public TextEncryptor tokenEncryptor(final CryptoProperties properties) {
        final AesGcmBytesEncryptor encryptor = AesGcmBytesEncryptor.withPassword(properties.tokenEncryptorPassword(), properties.tokenEncryptorSalt()).build();
        return new TextEncryptor() {
            @Override
            public @NullMarked String encrypt(String text) {
                return HexFormat.of().formatHex(encryptor.encrypt(text.getBytes(StandardCharsets.UTF_8)));
            }

            @Override
            public @NullMarked String decrypt(String encryptedText) {
                return new String(encryptor.decrypt(HexFormat.of().parseHex(encryptedText)), StandardCharsets.UTF_8);
            }
        };
    }
}