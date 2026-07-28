package com.mycomp.csmessage.accounts.service;

import com.mycomp.csmessage.accounts.dto.UserDetailsRequest;
import com.mycomp.csmessage.accounts.models.User;
import com.mycomp.csmessage.accounts.models.UserDetails;
import com.mycomp.csmessage.accounts.repository.UsersRepository;
import com.mycomp.csmessage.exceptions.BaseException;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UsersRepository userRepository;

  @Transactional
  public User updateUserDetails(String userId, UserDetailsRequest request) {
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

    return userRepository.save(user);
  }
}
