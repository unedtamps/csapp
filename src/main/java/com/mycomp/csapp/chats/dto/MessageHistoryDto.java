package com.mycomp.csapp.chats.dto;

import java.time.Instant;

public record MessageHistoryDto(
    String id, String conversationId, String senderId, String recipientId, String message,
    Instant timestamp) {}
