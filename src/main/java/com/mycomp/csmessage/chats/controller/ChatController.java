package com.mycomp.csmessage.chats.controller;

import com.mycomp.csmessage.chats.dto.ChatMessageDto;
import com.mycomp.csmessage.chats.service.ChatService;

import java.security.Principal;

import lombok.RequiredArgsConstructor;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatController {

  private final ChatService chatService;

  @MessageMapping("/chat.private")
  public void sendPrivateMessage(@Payload ChatMessageDto message, Principal principal) {
    chatService.sendPrivateMessage(message, principal.getName());
  }
}
