package com.mycomp.csmessage.chats.repository;

import com.mycomp.csmessage.chats.models.MessageHistory;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface MessageHistoryRepository extends MongoRepository<MessageHistory, String> {
  List<MessageHistory> findByConversationIdOrderByTimestampDesc(String conversationId);
}
