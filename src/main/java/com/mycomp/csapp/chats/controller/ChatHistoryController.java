package com.mycomp.csapp.chats.controller;

import com.mycomp.csapp.accounts.dto.AuthClaims;
import com.mycomp.csapp.chats.dto.ConversationSummaryDto;
import com.mycomp.csapp.chats.dto.MessageHistoryDto;
import com.mycomp.csapp.chats.service.ChatHistoryService;

import java.time.Instant;
import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatHistoryController {

  public static final int DEFAULT_LIMIT = 50;
  public static final int MAX_LIMIT = 100;

  private final ChatHistoryService chatHistoryService;

  @GetMapping("/history")
  public ResponseEntity<List<MessageHistoryDto>> getHistory(
      @AuthenticationPrincipal AuthClaims currentUser,
      @RequestParam String peerEmail,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          Instant before,
      @RequestParam(defaultValue = "50") int limit) {

    int cappedLimit = Math.min(Math.max(limit, 1), MAX_LIMIT);
    return ResponseEntity.ok(
        chatHistoryService.getHistory(currentUser.email(), peerEmail, before, cappedLimit));
  }

  @GetMapping("/conversations")
  public ResponseEntity<List<ConversationSummaryDto>> getConversations(
      @AuthenticationPrincipal AuthClaims currentUser) {

    return ResponseEntity.ok(chatHistoryService.getConversations(currentUser.email()));
  }
}
