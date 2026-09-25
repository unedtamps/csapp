package com.mycomp.csapp.chats.messaging.application;

import com.mycomp.csapp.chats.history.event.MessageSentEvent;
import com.mycomp.csapp.chats.messaging.payload.PrivateMessagePayload;

import io.github.springwolf.core.asyncapi.annotations.AsyncOperation;
import io.github.springwolf.core.asyncapi.annotations.AsyncPublisher;
import io.opentelemetry.instrumentation.annotations.WithSpan;

import java.time.Instant;

import lombok.RequiredArgsConstructor;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PrivateMessageService {

  private static final String MESSAGE_QUEUE_PREFIX = "/queue/messages.";

  private final SimpMessagingTemplate messagingTemplate;
  private final ApplicationEventPublisher eventPublisher;

  @AsyncPublisher(
      operation =
          @AsyncOperation(
              channelName = "/queue/messages.{recipientEmail}",
              description = "Private message delivered to a specific user queue",
               payloadType = PrivateMessagePayload.class))
  @WithSpan
  public void sendPrivateMessage(PrivateMessagePayload message, String senderEmail) {
    message.setSenderId(senderEmail);
    messagingTemplate.convertAndSend(MESSAGE_QUEUE_PREFIX + message.getRecipientId(), message);
    eventPublisher.publishEvent(
        new MessageSentEvent(
            senderEmail, message.getRecipientId(), message.getMessage(), Instant.now()));
  }
}
