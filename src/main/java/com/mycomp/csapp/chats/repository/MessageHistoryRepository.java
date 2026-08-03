package com.mycomp.csapp.chats.repository;

import com.mycomp.csapp.chats.models.MessageHistory;

import java.time.Instant;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MessageHistoryRepository extends MongoRepository<MessageHistory, String> {
  List<MessageHistory> findByConversationIdOrderByTimestampDesc(
      String conversationId, Pageable pageable);

  List<MessageHistory> findByConversationIdAndTimestampLessThanOrderByTimestampDesc(
      String conversationId, Instant before, Pageable pageable);

  List<MessageHistory> findBySenderIdOrRecipientIdOrderByTimestampDesc(
      String senderId, String recipientId);
}
