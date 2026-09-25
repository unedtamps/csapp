package com.mycomp.csapp.chats.history.api;

import com.mycomp.csapp.accounts.auth.principal.AuthenticatedUser;
import com.mycomp.csapp.chats.history.api.dto.ConversationSummaryResponse;
import com.mycomp.csapp.chats.history.api.dto.MessageHistoryResponse;
import com.mycomp.csapp.chats.history.application.MessageHistoryService;

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
public class MessageHistoryController {

  public static final int DEFAULT_LIMIT = 50;
  public static final int MAX_LIMIT = 100;

  private final MessageHistoryService messageHistoryService;

  @GetMapping("/history")
  public ResponseEntity<List<MessageHistoryResponse>> getHistory(
      @AuthenticationPrincipal AuthenticatedUser currentUser,
      @RequestParam String peerEmail,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          Instant before,
      @RequestParam(defaultValue = "" + DEFAULT_LIMIT) int limit) {

    int cappedLimit = Math.min(Math.max(limit, 1), MAX_LIMIT);
    return ResponseEntity.ok(
        messageHistoryService.getHistory(currentUser.email(), peerEmail, before, cappedLimit));
  }

  @GetMapping("/conversations")
  public ResponseEntity<List<ConversationSummaryResponse>> getConversations(
      @AuthenticationPrincipal AuthenticatedUser currentUser) {

    return ResponseEntity.ok(messageHistoryService.getConversations(currentUser.email()));
  }
}
