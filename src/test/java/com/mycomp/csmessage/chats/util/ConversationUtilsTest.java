package com.mycomp.csmessage.chats.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ConversationUtilsTest {

  @Test
  void conversationIdIsDeterministicAndOrderIndependent() {
    String ab = ConversationUtils.conversationId("alice@example.com", "bob@example.com");
    String ba = ConversationUtils.conversationId("bob@example.com", "alice@example.com");

    assertThat(ab).isEqualTo(ba);
    assertThat(ab).contains("alice@example.com").contains("bob@example.com");
  }
}
