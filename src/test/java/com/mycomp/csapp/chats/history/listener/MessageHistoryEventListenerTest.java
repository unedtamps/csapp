package com.mycomp.csapp.chats.history.listener;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import com.mycomp.csapp.chats.domain.ConversationIdGenerator;
import com.mycomp.csapp.chats.history.event.MessageSentEvent;
import com.mycomp.csapp.chats.history.persistence.mongodb.MessageHistoryDocument;
import com.mycomp.csapp.chats.history.persistence.mongodb.MessageHistoryRepository;

import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MessageHistoryEventListenerTest {

  @Mock private MessageHistoryRepository repository;

  private MessageHistoryEventListener listener;

  @BeforeEach
  void setUp() {
    listener = new MessageHistoryEventListener(repository);
  }

  @Test
  void onMessageSentPersistsHistoryWithCanonicalConversationId() {
    Instant timestamp = Instant.parse("2026-08-01T10:00:00Z");
    MessageSentEvent event =
        new MessageSentEvent("alice@example.com", "bob@example.com", "hi bob", timestamp);

    listener.onMessageSent(event);

    ArgumentCaptor<MessageHistoryDocument> captor =
        ArgumentCaptor.forClass(MessageHistoryDocument.class);
    verify(repository).save(captor.capture());

    MessageHistoryDocument saved = captor.getValue();
    assertThat(saved.getConversationId())
        .isEqualTo(
            ConversationIdGenerator.conversationId("alice@example.com", "bob@example.com"));
    assertThat(saved.getSenderId()).isEqualTo("alice@example.com");
    assertThat(saved.getRecipientId()).isEqualTo("bob@example.com");
    assertThat(saved.getMessage()).isEqualTo("hi bob");
    assertThat(saved.getTimestamp()).isEqualTo(timestamp);
  }
}
