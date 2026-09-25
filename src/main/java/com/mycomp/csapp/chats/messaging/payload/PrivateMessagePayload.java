package com.mycomp.csapp.chats.messaging.payload;

import lombok.Data;

@Data
public class PrivateMessagePayload {
  private String senderId;
  private String recipientId;
  private String message;
}
