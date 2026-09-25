package com.mycomp.csapp.accounts.persistence.jpa.entity;

import com.github.f4b6a3.ulid.UlidCreator;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity(name = "UserDetails")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "user_details")
public class UserDetailsEntity {
  @Builder.Default @Id private String id = UlidCreator.getUlid().toString();

  private String address;
  private String phoneNumber;
  private String identificationNumber;
  private String dateOfBirth;
  private String motherName;
}
