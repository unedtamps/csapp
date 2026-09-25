package com.mycomp.csapp.bootstrap.admin;

import com.mycomp.csapp.accounts.domain.Role;

import java.util.List;

import lombok.Data;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "spring.security")
@Data
public class AdminProperties {
  private List<AdminPropertiesData> admins;

  @Data
  public static class AdminPropertiesData {
    private String name;
    private String email;
    private String password;
    private Role role;
  }
}
