package com.mycomp.csmessage.chats.controller;

import com.mycomp.csmessage.chats.dto.ChatMessageDto;
import com.mycomp.csmessage.chats.dto.MessageSentEvent;

import io.github.springwolf.core.asyncapi.annotations.AsyncOperation;
import io.github.springwolf.core.asyncapi.annotations.AsyncPublisher;

import java.security.Principal;
import java.time.Instant;

import lombok.RequiredArgsConstructor;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatController {

  private final SimpMessagingTemplate messagingTemplate;
  private final ApplicationEventPublisher eventPublisher;

  @MessageMapping("/chat.private")
  @AsyncPublisher(
      operation =
          @AsyncOperation(
              channelName = "/user/{username}/queue/messages",
              description = "Private message delivered to a specific user",
              payloadType = ChatMessageDto.class))
  public void sendPrivateMessage(@Payload ChatMessageDto message, Principal principal) {

    String userId = principal.getName();
    message.setSenderId(userId);
    messagingTemplate.convertAndSendToUser(message.getRecipientId(), "/queue/messages", message);
    eventPublisher.publishEvent(
        new MessageSentEvent(
            userId, message.getRecipientId(), message.getMessage(), Instant.now()));
  }
}
