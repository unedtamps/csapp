package com.mycomp.csmessage.accounts.repository;

import com.mycomp.csmessage.accounts.models.User;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UsersRepository extends JpaRepository<User, String> {
  public boolean existsByEmail(String email);

  public boolean existsByUsername(String email);

  public User findByEmail(String email);
}
