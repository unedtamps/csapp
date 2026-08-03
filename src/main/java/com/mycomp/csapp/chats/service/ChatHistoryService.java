package com.mycomp.csapp.chats.service;

import com.mycomp.csapp.chats.dto.ConversationSummaryDto;
import com.mycomp.csapp.chats.dto.MessageHistoryDto;
import com.mycomp.csapp.chats.models.MessageHistory;
import com.mycomp.csapp.chats.repository.MessageHistoryRepository;
import com.mycomp.csapp.chats.util.ConversationUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatHistoryService {

  private final MessageHistoryRepository repository;

  /**
   * Returns the most recent {@code limit} messages exchanged between {@code userEmail} and {@code
   * peerEmail}, newest first. When {@code before} is set, only messages strictly older than it are
   * returned (cursor for infinite scroll).
   */
  public List<MessageHistoryDto> getHistory(String userEmail, String peerEmail, Instant before,
      int limit) {
    String conversationId = ConversationUtils.conversationId(userEmail, peerEmail);

    List<MessageHistory> messages =
        before == null
            ? repository.findByConversationIdOrderByTimestampDesc(
                conversationId, PageRequest.of(0, limit))
            : repository.findByConversationIdAndTimestampLessThanOrderByTimestampDesc(
                conversationId, before, PageRequest.of(0, limit));

    return messages.stream().map(ChatHistoryService::toDto).toList();
  }

  /** Lists the current user's conversations, most recently active first. */
  public List<ConversationSummaryDto> getConversations(String userEmail) {
    List<MessageHistory> messages =
        repository.findBySenderIdOrRecipientIdOrderByTimestampDesc(userEmail, userEmail);

    Map<String, MessageHistory> latestByConversation = new HashMap<>();
    for (MessageHistory message : messages) {
      latestByConversation.merge(
          message.getConversationId(), message, ChatHistoryService::keepNewer);
    }

    List<ConversationSummaryDto> conversations = new ArrayList<>();
    latestByConversation
        .values()
        .forEach(
            latest ->
                conversations.add(
                    new ConversationSummaryDto(
                        latest.getConversationId(),
                        peerOf(userEmail, latest),
                        latest.getMessage(),
                        latest.getTimestamp())));
    conversations.sort(
        (a, b) -> b.lastTimestamp().compareTo(a.lastTimestamp()));
    return conversations;
  }

  private static MessageHistory keepNewer(MessageHistory existing, MessageHistory candidate) {
    return existing.getTimestamp().isAfter(candidate.getTimestamp()) ? existing : candidate;
  }

  private static String peerOf(String userEmail, MessageHistory message) {
    return message.getSenderId().equals(userEmail) ? message.getRecipientId()
        : message.getSenderId();
  }

  private static MessageHistoryDto toDto(MessageHistory history) {
    return new MessageHistoryDto(
        history.getId(),
        history.getConversationId(),
        history.getSenderId(),
        history.getRecipientId(),
        history.getMessage(),
        history.getTimestamp());
  }
}
