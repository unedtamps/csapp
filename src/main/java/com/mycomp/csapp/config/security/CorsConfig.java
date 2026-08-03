package com.mycomp.csapp.config.security;

import java.util.List;

import lombok.Data;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.cors")
@Data
public class CorsConfig {
  private List<String> allowedOriginPatterns = List.of("https://mydomain.com");
}
