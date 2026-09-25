package com.mycomp.csapp.chats.history.application;

import com.mycomp.csapp.chats.domain.ConversationIdGenerator;
import com.mycomp.csapp.chats.history.api.dto.ConversationSummaryResponse;
import com.mycomp.csapp.chats.history.api.dto.MessageHistoryResponse;
import com.mycomp.csapp.chats.history.persistence.mongodb.MessageHistoryDocument;
import com.mycomp.csapp.chats.history.persistence.mongodb.MessageHistoryRepository;

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
public class MessageHistoryService {

  private final MessageHistoryRepository repository;

  /**
   * Returns the most recent {@code limit} messages exchanged between {@code userEmail} and {@code
   * peerEmail}, newest first. When {@code before} is set, only messages strictly older than it are
   * returned (cursor for infinite scroll).
   */
  public List<MessageHistoryResponse> getHistory(String userEmail, String peerEmail, Instant before,
      int limit) {
    String conversationId = ConversationIdGenerator.conversationId(userEmail, peerEmail);
    return findHistory(conversationId, before, limit).stream()
        .map(MessageHistoryService::toResponse)
        .toList();
  }

  private List<MessageHistoryDocument> findHistory(
      String conversationId, Instant before, int limit) {
    return before == null
        ? repository.findByConversationIdOrderByTimestampDesc(
            conversationId, PageRequest.of(0, limit))
        : repository.findByConversationIdAndTimestampLessThanOrderByTimestampDesc(
            conversationId, before, PageRequest.of(0, limit));
  }

  /** Lists the current user's conversations, most recently active first. */
  public List<ConversationSummaryResponse> getConversations(String userEmail) {
    List<MessageHistoryDocument> messages =
        repository.findBySenderIdOrRecipientIdOrderByTimestampDesc(userEmail, userEmail);

    Map<String, MessageHistoryDocument> latestByConversation = groupLatestByConversation(messages);
    return toConversationSummaries(userEmail, latestByConversation);
  }

  private static Map<String, MessageHistoryDocument> groupLatestByConversation(
      List<MessageHistoryDocument> messages) {
    Map<String, MessageHistoryDocument> latestByConversation = new HashMap<>();
    for (MessageHistoryDocument message : messages) {
      latestByConversation.merge(
          message.getConversationId(), message, MessageHistoryService::keepNewer);
    }
    return latestByConversation;
  }

  private static List<ConversationSummaryResponse> toConversationSummaries(
      String userEmail, Map<String, MessageHistoryDocument> latestByConversation) {
    List<ConversationSummaryResponse> conversations = new ArrayList<>();
    latestByConversation
        .values()
        .forEach(
            latest ->
                conversations.add(
                    new ConversationSummaryResponse(
                        latest.getConversationId(),
                        peerOf(userEmail, latest),
                        latest.getMessage(),
                        latest.getTimestamp())));
    conversations.sort(
        (a, b) -> b.lastTimestamp().compareTo(a.lastTimestamp()));
    return conversations;
  }

  private static MessageHistoryDocument keepNewer(
      MessageHistoryDocument existing, MessageHistoryDocument candidate) {
    return existing.getTimestamp().isAfter(candidate.getTimestamp()) ? existing : candidate;
  }

  private static String peerOf(String userEmail, MessageHistoryDocument message) {
    return message.getSenderId().equals(userEmail) ? message.getRecipientId()
        : message.getSenderId();
  }

  private static MessageHistoryResponse toResponse(MessageHistoryDocument history) {
    return new MessageHistoryResponse(
        history.getId(),
        history.getConversationId(),
        history.getSenderId(),
        history.getRecipientId(),
        history.getMessage(),
        history.getTimestamp());
  }
}
