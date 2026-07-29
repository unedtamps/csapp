package com.mycomp.csmessage.config.admin;

import com.mycomp.csmessage.accounts.models.Role;

import java.util.List;

import lombok.Data;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "spring.security")
@Data
public class AdminConfig {
  private List<AdminConfigData> admins;

  @Data
  public static class AdminConfigData {
    private String name;
    private String email;
    private String password;
    private Role role;
  }
}
