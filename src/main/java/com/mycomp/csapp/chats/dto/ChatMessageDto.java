package com.mycomp.csapp.chats.dto;

import lombok.Data;

@Data
public class ChatMessageDto {
  private String senderId;
  private String recipientId;
  private String message;
}
