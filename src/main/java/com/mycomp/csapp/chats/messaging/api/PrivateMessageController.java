package com.mycomp.csapp.chats.messaging.api;

import com.mycomp.csapp.chats.messaging.application.PrivateMessageService;
import com.mycomp.csapp.chats.messaging.payload.PrivateMessagePayload;

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
public class PrivateMessageController {

  private final PrivateMessageService privateMessageService;

  /**
   * Handles STOMP frames sent to {@code /app/chat.private} and delegates delivery to {@link
   * com.mycomp.csapp.chats.messaging.application.PrivateMessageService}.
   *
   * @param message inbound private message (senderId is overwritten from the principal)
   * @param principal authenticated user resolved by {@code StompAuthenticationInterceptor} on CONNECT
   */
   @MessageMapping("/chat.private")
  public void sendPrivateMessage(@Payload PrivateMessagePayload message, Principal principal) {
    privateMessageService.sendPrivateMessage(message, principal.getName());
  }
}
