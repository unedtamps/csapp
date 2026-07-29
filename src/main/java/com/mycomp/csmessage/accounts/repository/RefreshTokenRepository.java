package com.mycomp.csmessage.accounts.repository;

import com.mycomp.csmessage.accounts.models.RefreshToken;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {

  @Modifying
  @Query("DELETE FROM RefreshToken rt WHERE rt.id = :id")
  int deleteByIdReturningCount(@Param("id") String id);
}
