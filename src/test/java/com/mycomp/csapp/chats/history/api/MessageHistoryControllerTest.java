package com.mycomp.csapp.chats.history.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mycomp.csapp.accounts.auth.principal.AuthenticatedUser;
import com.mycomp.csapp.accounts.domain.Role;
import com.mycomp.csapp.chats.history.application.MessageHistoryService;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class MessageHistoryControllerTest {

  @Mock private MessageHistoryService messageHistoryService;

  private MessageHistoryController controller;

  @BeforeEach
  void setUp() {
    controller = new MessageHistoryController(messageHistoryService);
  }

  @Test
  void historyLimitIsCappedAtTheMaximum() {
    AuthenticatedUser currentUser = new AuthenticatedUser("u1", "alice@example.com", Role.USER);
    when(messageHistoryService.getHistory("alice@example.com", "bob@example.com", null, 100))
        .thenReturn(List.of());

    var response = controller.getHistory(currentUser, "bob@example.com", null, 1000);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    verify(messageHistoryService).getHistory("alice@example.com", "bob@example.com", null, 100);
  }

  @Test
  void historyLimitIsFlooredAtOne() {
    AuthenticatedUser currentUser = new AuthenticatedUser("u1", "alice@example.com", Role.USER);
    when(messageHistoryService.getHistory("alice@example.com", "bob@example.com", null, 1))
        .thenReturn(List.of());

    controller.getHistory(currentUser, "bob@example.com", null, 0);

    verify(messageHistoryService).getHistory("alice@example.com", "bob@example.com", null, 1);
  }
}
