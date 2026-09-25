package com.mycomp.csapp.chats.history.listener;

import com.mycomp.csapp.chats.domain.ConversationIdGenerator;
import com.mycomp.csapp.chats.history.event.MessageSentEvent;
import com.mycomp.csapp.chats.history.persistence.mongodb.MessageHistoryDocument;
import com.mycomp.csapp.chats.history.persistence.mongodb.MessageHistoryRepository;

import io.opentelemetry.instrumentation.annotations.WithSpan;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class MessageHistoryEventListener {

  private final MessageHistoryRepository repository;

  @Async
  @EventListener
  @WithSpan
  public void onMessageSent(MessageSentEvent event) {
    MessageHistoryDocument history = toDocument(event);
    repository.save(history);
    log.info("Message saved to history: {}", history.getId());
  }

  private static MessageHistoryDocument toDocument(MessageSentEvent event) {
    return MessageHistoryDocument.builder()
        .conversationId(
            ConversationIdGenerator.conversationId(event.senderId(), event.recipientId()))
        .senderId(event.senderId())
        .recipientId(event.recipientId())
        .message(event.message())
        .timestamp(event.timestamp())
        .build();
  }
}
