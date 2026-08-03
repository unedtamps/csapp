package com.mycomp.csapp.chats.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import com.mycomp.csapp.chats.dto.MessageSentEvent;
import com.mycomp.csapp.chats.models.MessageHistory;
import com.mycomp.csapp.chats.repository.MessageHistoryRepository;
import com.mycomp.csapp.chats.util.ConversationUtils;

import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MessageHistoryListenerTest {

  @Mock private MessageHistoryRepository repository;

  private MessageHistoryListener listener;

  @BeforeEach
  void setUp() {
    listener = new MessageHistoryListener(repository);
  }

  @Test
  void onMessageSentPersistsHistoryWithCanonicalConversationId() {
    Instant timestamp = Instant.parse("2026-08-01T10:00:00Z");
    MessageSentEvent event =
        new MessageSentEvent("alice@example.com", "bob@example.com", "hi bob", timestamp);

    listener.onMessageSent(event);

    ArgumentCaptor<MessageHistory> captor = ArgumentCaptor.forClass(MessageHistory.class);
    verify(repository).save(captor.capture());

    MessageHistory saved = captor.getValue();
    assertThat(saved.getConversationId())
        .isEqualTo(ConversationUtils.conversationId("alice@example.com", "bob@example.com"));
    assertThat(saved.getSenderId()).isEqualTo("alice@example.com");
    assertThat(saved.getRecipientId()).isEqualTo("bob@example.com");
    assertThat(saved.getMessage()).isEqualTo("hi bob");
    assertThat(saved.getTimestamp()).isEqualTo(timestamp);
  }
}
