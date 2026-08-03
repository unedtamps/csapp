package com.mycomp.csapp.seeder;

import com.mycomp.csapp.accounts.models.Role;
import com.mycomp.csapp.accounts.models.User;
import com.mycomp.csapp.accounts.repository.UsersRepository;

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
public class DataDevSeeder implements CommandLineRunner {

  public final UsersRepository usersRepository;
  public final PasswordEncoder passwordEncoder;

  @Override
  @Transactional
  public void run(String... args) {
    seedDefaultAdmin();
  }

  private void seedDefaultAdmin() {
    if (usersRepository.existsByEmail("admin@example.com")) {
      log.info("Admin already exists");
      return;
    }

    log.info("Creating default admin");
    usersRepository.save(
        User.builder()
            .email("admin@example.com")
            .username("admin")
            .password(passwordEncoder.encode("password"))
            .role(Role.ADMIN)
            .build());

    log.info("Default admin created");
  }
}
