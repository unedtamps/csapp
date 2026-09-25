package com.mycomp.csapp.accounts.persistence.jpa.repository;

import com.mycomp.csapp.accounts.persistence.jpa.entity.RefreshTokenEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, String> {

  @Modifying
  @Query("DELETE FROM RefreshToken rt WHERE rt.id = :id")
  int deleteByIdReturningCount(@Param("id") String id);
}
