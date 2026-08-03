package com.mycomp.csapp.chats.models;

import com.github.f4b6a3.ulid.UlidCreator;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "message_history")
@CompoundIndex(name = "conv_ts", def = "{'conversationId': 1, 'timestamp': -1}")
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MessageHistory {

  @Builder.Default @Id private String id = UlidCreator.getUlid().toString();
  private String conversationId;
  private String senderId;
  private String recipientId;
  private String message;
  private Instant timestamp;
}
