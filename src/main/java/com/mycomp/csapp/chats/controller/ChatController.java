package com.mycomp.csapp.chats.controller;

import com.mycomp.csapp.chats.dto.ChatMessageDto;
import com.mycomp.csapp.chats.service.ChatService;

import java.security.Principal;

import lombok.RequiredArgsConstructor;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

/**
 * STOMP controller. {@link org.springframework.messaging.handler.annotation.MessageMapping} methods
 * handle inbound frames routed to {@code /app/**} by the WebSocket config (broker relay). Thin
 * adapter - no business logic.
 */
@Controller
@RequiredArgsConstructor
public class ChatController {

  private final ChatService chatService;

  /**
   * Handles STOMP frames sent to {@code /app/chat.private} and delegates delivery to {@link
   * com.mycomp.csapp.chats.service.ChatService}.
   *
   * @param message inbound private message (senderId is overwritten from the principal)
   * @param principal authenticated user resolved by {@code StompAuthInterceptor} on CONNECT
   */
  @MessageMapping("/chat.private")
  public void sendPrivateMessage(@Payload ChatMessageDto message, Principal principal) {
    chatService.sendPrivateMessage(message, principal.getName());
  }
}
