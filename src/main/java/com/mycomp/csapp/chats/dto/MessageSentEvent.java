package com.mycomp.csapp.chats.dto;

import java.time.Instant;

public record MessageSentEvent(String senderId, String recipientId, String message, Instant timestamp) {}
