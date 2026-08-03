package com.mycomp.csapp.chats.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mycomp.csapp.chats.dto.ConversationSummaryDto;
import com.mycomp.csapp.chats.dto.MessageHistoryDto;
import com.mycomp.csapp.chats.models.MessageHistory;
import com.mycomp.csapp.chats.repository.MessageHistoryRepository;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class ChatHistoryServiceTest {

  @Mock private MessageHistoryRepository repository;

  private ChatHistoryService chatHistoryService;

  @BeforeEach
  void setUp() {
    chatHistoryService = new ChatHistoryService(repository);
  }

  private static MessageHistory message(
      String conversationId, String sender, String recipient, String content, Instant timestamp) {
    return MessageHistory.builder()
        .id("id-" + timestamp.toEpochMilli())
        .conversationId(conversationId)
        .senderId(sender)
        .recipientId(recipient)
        .message(content)
        .timestamp(timestamp)
        .build();
  }

  @Test
  void getHistoryMapsMessagesNewestFirst() {
    Instant t1 = Instant.parse("2026-08-01T10:00:00Z");
    Instant t2 = Instant.parse("2026-08-01T10:05:00Z");
    MessageHistory newer = message("alice_bob", "bob@example.com", "alice@example.com", "hi", t2);
    MessageHistory older = message("alice_bob", "alice@example.com", "bob@example.com", "yo", t1);

    when(repository.findByConversationIdOrderByTimestampDesc(
            eq("alice@example.com_bob@example.com"), any(PageRequest.class)))
        .thenReturn(List.of(newer, older));

    List<MessageHistoryDto> history =
        chatHistoryService.getHistory("alice@example.com", "bob@example.com", null, 50);

    assertThat(history).hasSize(2);
    assertThat(history.get(0).message()).isEqualTo("hi");
    assertThat(history.get(0).senderId()).isEqualTo("bob@example.com");
    assertThat(history.get(1).message()).isEqualTo("yo");
    verify(repository)
        .findByConversationIdOrderByTimestampDesc(
            eq("alice@example.com_bob@example.com"), eq(PageRequest.of(0, 50)));
  }

  @Test
  void getHistoryWithBeforeCursorUsesTimestampFilter() {
    Instant before = Instant.parse("2026-08-01T10:00:00Z");

    when(repository.findByConversationIdAndTimestampLessThanOrderByTimestampDesc(
            any(), any(), any()))
        .thenReturn(List.of());

    chatHistoryService.getHistory("alice@example.com", "bob@example.com", before, 20);

    verify(repository)
        .findByConversationIdAndTimestampLessThanOrderByTimestampDesc(
            eq("alice@example.com_bob@example.com"), eq(before), eq(PageRequest.of(0, 20)));
  }

  @Test
  void getConversationsGroupsByConversationAndKeepsLatest() {
    Instant t1 = Instant.parse("2026-08-01T10:00:00Z");
    Instant t2 = Instant.parse("2026-08-01T10:05:00Z");
    Instant t3 = Instant.parse("2026-08-01T09:00:00Z");

    MessageHistory aliceToBob =
        message(
            "alice@example.com_bob@example.com",
            "alice@example.com",
            "bob@example.com",
            "first",
            t1);
    MessageHistory bobToAlice =
        message(
            "alice@example.com_bob@example.com",
            "bob@example.com",
            "alice@example.com",
            "second",
            t2);
    MessageHistory carolToAlice =
        message(
            "alice@example.com_carol@example.com",
            "carol@example.com",
            "alice@example.com",
            "hey carol",
            t3);

    when(repository.findBySenderIdOrRecipientIdOrderByTimestampDesc(
            "alice@example.com", "alice@example.com"))
        .thenReturn(List.of(aliceToBob, bobToAlice, carolToAlice));

    List<ConversationSummaryDto> conversations =
        chatHistoryService.getConversations("alice@example.com");

    assertThat(conversations).hasSize(2);
    assertThat(conversations.get(0).peerEmail()).isEqualTo("bob@example.com");
    assertThat(conversations.get(0).lastMessage()).isEqualTo("second");
    assertThat(conversations.get(0).lastTimestamp()).isEqualTo(t2);
    assertThat(conversations.get(1).peerEmail()).isEqualTo("carol@example.com");
    assertThat(conversations.get(1).lastMessage()).isEqualTo("hey carol");
  }
}
