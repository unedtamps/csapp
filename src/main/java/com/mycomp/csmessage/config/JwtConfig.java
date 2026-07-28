package com.mycomp.csmessage.config;

import lombok.Data;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "spring.security.jwt")
@Data
public class JwtConfig {
  private String secret;
  private long accessTokenExpiration = 900000;
  private long refreshTokenExpiration = 86400000;
}
