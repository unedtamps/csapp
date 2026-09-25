package com.mycomp.csapp.accounts.auth.jwt;

import lombok.Data;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "spring.security.jwt")
@Data
public class JwtProperties {
  private String secret;
  private long accessTokenExpiration = 900000;
  private long refreshTokenExpiration = 86400000;
}
