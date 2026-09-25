package com.mycomp.csapp.accounts.application;

import com.mycomp.csapp.accounts.api.user.dto.UserDetailsRequest;
import com.mycomp.csapp.accounts.api.user.dto.UserResponse;
import com.mycomp.csapp.accounts.api.user.dto.UserSummaryResponse;
import com.mycomp.csapp.accounts.persistence.jpa.entity.UserDetailsEntity;
import com.mycomp.csapp.accounts.persistence.jpa.entity.UserEntity;
import com.mycomp.csapp.accounts.persistence.jpa.repository.UserRepository;
import com.mycomp.csapp.shared.error.ApplicationException;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserAccountService {

  private final UserRepository userRepository;

  public UserSummaryResponse findByEmail(String email) {
    UserEntity user = userRepository.findByEmail(email);
    if (user == null) {
      throw new ApplicationException(HttpStatus.NOT_FOUND, "User not found");
    }
    return toSummary(user);
  }

  @Transactional
  public UserResponse updateUserDetails(String userId, UserDetailsRequest request) {
    UserEntity user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ApplicationException(HttpStatus.NOT_FOUND, "User not found"));

    if (user.getDetails() == null) {
      user.setDetails(toDetails(request));
    } else {
      applyDetails(user.getDetails(), request);
    }

    return toResponse(userRepository.save(user));
  }

  private static UserDetailsEntity toDetails(UserDetailsRequest request) {
    return UserDetailsEntity.builder()
        .address(request.address())
        .phoneNumber(request.phoneNumber())
        .identificationNumber(request.identificationNumber())
        .dateOfBirth(request.dateOfBirth())
        .motherName(request.motherName())
        .build();
  }

  private static void applyDetails(UserDetailsEntity details, UserDetailsRequest request) {
    details.setAddress(request.address());
    details.setPhoneNumber(request.phoneNumber());
    details.setIdentificationNumber(request.identificationNumber());
    details.setDateOfBirth(request.dateOfBirth());
    details.setMotherName(request.motherName());
  }

  private UserResponse toResponse(UserEntity user) {
    return new UserResponse(
        user.getId(), user.getUsername(), user.getEmail(), user.getRole(), user.getDetails());
  }

  private UserSummaryResponse toSummary(UserEntity user) {
    return new UserSummaryResponse(
        user.getId(), user.getUsername(), user.getEmail(), user.getRole());
  }
}
