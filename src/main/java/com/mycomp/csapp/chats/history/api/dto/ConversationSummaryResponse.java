package com.mycomp.csapp.chats.history.api.dto;

import java.time.Instant;

public record ConversationSummaryResponse(
    String conversationId, String peerEmail, String lastMessage, Instant lastTimestamp) {}
