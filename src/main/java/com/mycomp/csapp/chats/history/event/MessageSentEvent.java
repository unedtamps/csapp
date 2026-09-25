package com.mycomp.csapp.chats.history.event;

import java.time.Instant;

public record MessageSentEvent(String senderId, String recipientId, String message, Instant timestamp) {}
