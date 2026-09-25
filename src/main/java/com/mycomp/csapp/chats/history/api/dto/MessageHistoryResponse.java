package com.mycomp.csapp.chats.history.api.dto;

import java.time.Instant;

public record MessageHistoryResponse(
    String id, String conversationId, String senderId, String recipientId, String message,
    Instant timestamp) {}
