package com.mycomp.csapp.accounts.service;

import com.mycomp.csapp.accounts.dto.UserDetailsRequest;
import com.mycomp.csapp.accounts.dto.UserResponse;
import com.mycomp.csapp.accounts.dto.UserSummaryResponse;
import com.mycomp.csapp.accounts.models.User;
import com.mycomp.csapp.accounts.models.UserDetails;
import com.mycomp.csapp.accounts.repository.UsersRepository;
import com.mycomp.csapp.exceptions.BaseException;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UsersRepository userRepository;

  public UserSummaryResponse findByEmail(String email) {
    User user = userRepository.findByEmail(email);
    if (user == null) {
      throw new BaseException(HttpStatus.NOT_FOUND, "User not found");
    }
    return toSummary(user);
  }

  @Transactional
  public UserResponse updateUserDetails(String userId, UserDetailsRequest request) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new BaseException(HttpStatus.NOT_FOUND, "User not found"));

    if (user.getDetails() == null) {
      UserDetails newDetails =
          UserDetails.builder()
              .address(request.address())
              .phoneNumber(request.phoneNumber())
              .identificationNumber(request.identificationNumber())
              .dateOfBirth(request.dateOfBirth())
              .motherName(request.motherName())
              .build();
      user.setDetails(newDetails);
    } else {
      UserDetails existingDetails = user.getDetails();
      existingDetails.setAddress(request.address());
      existingDetails.setPhoneNumber(request.phoneNumber());
      existingDetails.setIdentificationNumber(request.identificationNumber());
      existingDetails.setDateOfBirth(request.dateOfBirth());
      existingDetails.setMotherName(request.motherName());
    }

    return toResponse(userRepository.save(user));
  }

  private UserResponse toResponse(User user) {
    return new UserResponse(
        user.getId(), user.getUsername(), user.getEmail(), user.getRole(), user.getDetails());
  }

  private UserSummaryResponse toSummary(User user) {
    return new UserSummaryResponse(
        user.getId(), user.getUsername(), user.getEmail(), user.getRole());
  }
}
