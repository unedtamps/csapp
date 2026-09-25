package com.mycomp.csapp.accounts.persistence.jpa.repository;

import com.mycomp.csapp.accounts.persistence.jpa.entity.UserEntity;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity, String> {
  public boolean existsByEmail(String email);

  public boolean existsByUsername(String username);

  public UserEntity findByEmail(String email);
}
