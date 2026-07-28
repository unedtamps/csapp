package com.mycomp.csmessage.chats.dto;

import java.time.Instant;

public record MessageSentEvent(String senderId, String recipientId, String message, Instant timestamp) {}
