package com.nxt.user_service.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "jwt")
public class JwtConfig {
    private String secret;
    private long accessExpirationSeconds;
    private long refreshExpirationSeconds;
    private String algorithm;
    private String rsaPrivateKeyPath;
    private String rsaPublicKeyPath;
}