package com.mycomp.csapp.chats.util;

import java.util.stream.Stream;

public class ConversationUtils {

  private ConversationUtils() {}

  public static String conversationId(String userA, String userB) {
    return Stream.of(userA, userB).sorted().collect(java.util.stream.Collectors.joining("_"));
  }
}
