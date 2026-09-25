package com.mycomp.csapp.bootstrap.admin;

import com.mycomp.csapp.accounts.domain.Role;
import com.mycomp.csapp.accounts.persistence.jpa.entity.UserEntity;
import com.mycomp.csapp.accounts.persistence.jpa.repository.UserRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Profile("!prod")
@RequiredArgsConstructor
public class DevelopmentAdminSeeder implements CommandLineRunner {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  @Override
  @Transactional
  public void run(String... args) {
    seedDefaultAdmin();
  }

  private void seedDefaultAdmin() {
    if (userRepository.existsByEmail("admin@example.com")) {
      log.info("Admin already exists");
      return;
    }

    log.info("Creating default admin");
    userRepository.save(
        UserEntity.builder()
            .email("admin@example.com")
            .username("admin")
            .password(passwordEncoder.encode("password"))
            .role(Role.ADMIN)
            .build());

    log.info("Default admin created");
  }
}
