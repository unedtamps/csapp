package com.mycomp.csapp.chats.history.persistence.mongodb;

import com.mycomp.csapp.chats.history.persistence.mongodb.MessageHistoryDocument;

import java.time.Instant;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MessageHistoryRepository extends MongoRepository<MessageHistoryDocument, String> {
  List<MessageHistoryDocument> findByConversationIdOrderByTimestampDesc(
      String conversationId, Pageable pageable);

  List<MessageHistoryDocument> findByConversationIdAndTimestampLessThanOrderByTimestampDesc(
      String conversationId, Instant before, Pageable pageable);

  List<MessageHistoryDocument> findBySenderIdOrRecipientIdOrderByTimestampDesc(
      String senderId, String recipientId);
}
