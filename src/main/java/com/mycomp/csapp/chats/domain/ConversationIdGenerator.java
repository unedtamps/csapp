package com.mycomp.csapp.chats.domain;

import java.util.stream.Stream;

public class ConversationIdGenerator {

  private ConversationIdGenerator() {}

  public static String conversationId(String userA, String userB) {
    return Stream.of(userA, userB).sorted().collect(java.util.stream.Collectors.joining("_"));
  }
}
