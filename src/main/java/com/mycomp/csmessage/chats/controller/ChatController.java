package com.mycomp.csmessage.chats.controller;

import com.mycomp.csmessage.chats.dto.ChatMessageDto;

import java.security.Principal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatController {

  private final SimpMessagingTemplate messagingTemplate;

  @MessageMapping("/chat.private")
  public void sendPrivateMessage(@Payload ChatMessageDto message, Principal principal) {

    String userId = principal.getName();
    log.info("Received private message from {}", userId);
    message.setSenderId(userId);

    log.info("Sending private message from {} to {}", userId, message.getRecipientId());

    messagingTemplate.convertAndSendToUser(message.getRecipientId(), "/queue/messages", message);
  }
}
