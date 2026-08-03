package com.mycomp.csapp.chats.service;

import com.mycomp.csapp.chats.dto.ChatMessageDto;
import com.mycomp.csapp.chats.dto.MessageSentEvent;

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
public class ChatService {

  private final SimpMessagingTemplate messagingTemplate;
  private final ApplicationEventPublisher eventPublisher;

  @AsyncPublisher(
      operation =
          @AsyncOperation(
              channelName = "/queue/messages.{recipientEmail}",
              description = "Private message delivered to a specific user queue",
              payloadType = ChatMessageDto.class))
  @WithSpan
  public void sendPrivateMessage(ChatMessageDto message, String senderEmail) {
    message.setSenderId(senderEmail);
    messagingTemplate.convertAndSend("/queue/messages." + message.getRecipientId(), message);
    eventPublisher.publishEvent(
        new MessageSentEvent(
            senderEmail, message.getRecipientId(), message.getMessage(), Instant.now()));
  }
}
