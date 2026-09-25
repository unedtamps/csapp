package com.mycomp.csapp.chats.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ConversationIdGeneratorTest {

  @Test
  void conversationIdIsDeterministicAndOrderIndependent() {
    String ab = ConversationIdGenerator.conversationId("alice@example.com", "bob@example.com");
    String ba = ConversationIdGenerator.conversationId("bob@example.com", "alice@example.com");

    assertThat(ab).isEqualTo(ba);
    assertThat(ab).contains("alice@example.com").contains("bob@example.com");
  }
}
