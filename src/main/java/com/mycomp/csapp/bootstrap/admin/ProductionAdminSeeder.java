package com.mycomp.csapp.bootstrap.admin;

import com.mycomp.csapp.accounts.persistence.jpa.entity.UserEntity;
import com.mycomp.csapp.accounts.persistence.jpa.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@Profile("prod")
@RequiredArgsConstructor
public class ProductionAdminSeeder implements CommandLineRunner {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final AdminProperties adminProperties;

  @Override
  @Transactional
  public void run(String... args) {
    try {
      for (AdminProperties.AdminPropertiesData admin : adminProperties.getAdmins()) {
        if (!userRepository.existsByEmail(admin.getEmail())) {
          userRepository.save(
              UserEntity.builder()
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
}
