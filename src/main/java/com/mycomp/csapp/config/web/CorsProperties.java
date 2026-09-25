package com.mycomp.csapp.config.web;

import java.util.List;

import lombok.Data;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.cors")
@Data
public class CorsProperties {
  private List<String> allowedOriginPatterns = List.of("https://mydomain.com");
}
