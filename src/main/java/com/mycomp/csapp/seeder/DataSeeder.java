package com.mycomp.csapp.seeder;

import com.mycomp.csapp.accounts.models.Role;
import com.mycomp.csapp.accounts.models.User;
import com.mycomp.csapp.accounts.repository.UsersRepository;
import com.mycomp.csapp.config.admin.AdminConfig;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
@Profile("prod")
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

  private final UsersRepository usersRepository;
  private final PasswordEncoder passwordEncoder;
  private final ObjectMapper objectMapper;
  private final AdminConfig adminConfig;

  @Override
  @Transactional
  public void run(String... args) {
    try {
      for (AdminConfig.AdminConfigData admin : adminConfig.getAdmins()) {
        if (!usersRepository.existsByEmail(admin.getEmail())) {
          usersRepository.save(
              User.builder()
                  .email(admin.getEmail())
                  .username(admin.getName())
                  .password(passwordEncoder.encode(admin.getPassword()))
                  .role(admin.getRole())
                  .build());
          log.info("Admin {} created", admin.getEmail());
        }
      }
    } catch (Exception e) {
      log.error("Failed to seed production admin data from JSON", e);
    }
  }

  public record AdminSeedDto(String username, String email, String password, Role role) {}
}
