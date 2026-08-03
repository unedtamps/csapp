package com.mycomp.csapp.chats.dto;

import java.time.Instant;

public record ConversationSummaryDto(
    String conversationId, String peerEmail, String lastMessage, Instant lastTimestamp) {}
