package com.mycomp.csapp.chats.service;

import com.mycomp.csapp.chats.dto.MessageSentEvent;
import com.mycomp.csapp.chats.models.MessageHistory;
import com.mycomp.csapp.chats.repository.MessageHistoryRepository;
import com.mycomp.csapp.chats.util.ConversationUtils;

import io.opentelemetry.instrumentation.annotations.WithSpan;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class MessageHistoryListener {

  private final MessageHistoryRepository repository;

  @Async
  @EventListener
  @WithSpan
  public void onMessageSent(MessageSentEvent event) {
    MessageHistory history =
        MessageHistory.builder()
            .conversationId(ConversationUtils.conversationId(event.senderId(), event.recipientId()))
            .senderId(event.senderId())
            .recipientId(event.recipientId())
            .message(event.message())
            .timestamp(event.timestamp())
            .build();
    repository.save(history);
    log.info("Message saved to history: {}", history.getId());
  }
}
