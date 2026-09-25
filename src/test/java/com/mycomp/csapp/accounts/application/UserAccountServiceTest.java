package com.mycomp.csapp.accounts.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mycomp.csapp.accounts.api.user.dto.UserDetailsRequest;
import com.mycomp.csapp.accounts.domain.Role;
import com.mycomp.csapp.accounts.persistence.jpa.entity.UserDetailsEntity;
import com.mycomp.csapp.accounts.persistence.jpa.entity.UserEntity;
import com.mycomp.csapp.accounts.persistence.jpa.repository.UserRepository;
import com.mycomp.csapp.shared.error.ApplicationException;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class UserAccountServiceTest {

  @Mock private UserRepository userRepository;

  private UserAccountService userAccountService;

  @BeforeEach
  void setUp() {
    userAccountService = new UserAccountService(userRepository);
  }

  @Test
  void findByEmailMapsTheUserSummary() {
    UserEntity user = user();
    when(userRepository.findByEmail(user.getEmail())).thenReturn(user);

    var result = userAccountService.findByEmail(user.getEmail());

    assertThat(result.id()).isEqualTo(user.getId());
    assertThat(result.username()).isEqualTo(user.getUsername());
    assertThat(result.email()).isEqualTo(user.getEmail());
    assertThat(result.role()).isEqualTo(Role.USER);
  }

  @Test
  void findByEmailRejectsAnUnknownUser() {
    when(userRepository.findByEmail("unknown@example.com")).thenReturn(null);

    assertThatThrownBy(() -> userAccountService.findByEmail("unknown@example.com"))
        .isInstanceOf(ApplicationException.class)
        .hasMessage("User not found")
        .satisfies(
            exception ->
                assertThat(((ApplicationException) exception).getStatus())
                    .isEqualTo(HttpStatus.NOT_FOUND));
  }

  @Test
  void updateUserDetailsCreatesDetailsWhenTheyDoNotExist() {
    UserEntity user = user();
    UserDetailsRequest request = request();
    when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
    when(userRepository.save(any(UserEntity.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    var result = userAccountService.updateUserDetails(user.getId(), request);

    ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
    verify(userRepository).save(captor.capture());
    UserDetailsEntity savedDetails = captor.getValue().getDetails();
    assertThat(savedDetails.getAddress()).isEqualTo(request.address());
    assertThat(savedDetails.getPhoneNumber()).isEqualTo(request.phoneNumber());
    assertThat(savedDetails.getIdentificationNumber())
        .isEqualTo(request.identificationNumber());
    assertThat(savedDetails.getDateOfBirth()).isEqualTo(request.dateOfBirth());
    assertThat(savedDetails.getMotherName()).isEqualTo(request.motherName());
    assertThat(result.details()).isSameAs(savedDetails);
  }

  @Test
  void updateUserDetailsUpdatesExistingDetails() {
    UserEntity user = user();
    user.setDetails(UserDetailsEntity.builder().address("old address").build());
    UserDetailsRequest request = request();
    when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
    when(userRepository.save(any(UserEntity.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    userAccountService.updateUserDetails(user.getId(), request);

    assertThat(user.getDetails().getAddress()).isEqualTo(request.address());
    assertThat(user.getDetails().getPhoneNumber()).isEqualTo(request.phoneNumber());
    assertThat(user.getDetails().getIdentificationNumber())
        .isEqualTo(request.identificationNumber());
    assertThat(user.getDetails().getDateOfBirth()).isEqualTo(request.dateOfBirth());
    assertThat(user.getDetails().getMotherName()).isEqualTo(request.motherName());
  }

  private static UserEntity user() {
    return UserEntity.builder()
        .id("u1")
        .username("alice")
        .email("alice@example.com")
        .password("encoded-password")
        .role(Role.USER)
        .build();
  }

  private static UserDetailsRequest request() {
    return new UserDetailsRequest(
        "new address", "+1234567890", "id-123", "2000-01-01", "mother");
  }
}
