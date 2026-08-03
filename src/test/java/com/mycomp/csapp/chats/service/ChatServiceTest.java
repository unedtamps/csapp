package com.mycomp.csapp.chats.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import com.mycomp.csapp.chats.dto.ChatMessageDto;
import com.mycomp.csapp.chats.dto.MessageSentEvent;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

  @Mock private SimpMessagingTemplate messagingTemplate;
  @Mock private ApplicationEventPublisher eventPublisher;

  private ChatService chatService;

  @BeforeEach
  void setUp() {
    chatService = new ChatService(messagingTemplate, eventPublisher);
  }

  @Test
  void sendPrivateMessageOverwritesSenderIdAndDeliversToRecipientQueue() {
    ChatMessageDto message = new ChatMessageDto();
    message.setRecipientId("bob@example.com");
    message.setMessage("hi bob");

    chatService.sendPrivateMessage(message, "alice@example.com");

    ArgumentCaptor<ChatMessageDto> payloadCaptor = ArgumentCaptor.forClass(ChatMessageDto.class);
    verify(messagingTemplate)
        .convertAndSend(eq("/queue/messages.bob@example.com"), payloadCaptor.capture());

    ChatMessageDto delivered = payloadCaptor.getValue();
    assertThat(delivered.getSenderId()).isEqualTo("alice@example.com");
    assertThat(delivered.getRecipientId()).isEqualTo("bob@example.com");
    assertThat(delivered.getMessage()).isEqualTo("hi bob");

    ArgumentCaptor<MessageSentEvent> eventCaptor = ArgumentCaptor.forClass(MessageSentEvent.class);
    verify(eventPublisher).publishEvent(eventCaptor.capture());

    MessageSentEvent event = eventCaptor.getValue();
    assertThat(event.senderId()).isEqualTo("alice@example.com");
    assertThat(event.recipientId()).isEqualTo("bob@example.com");
    assertThat(event.message()).isEqualTo("hi bob");
    assertThat(event.timestamp()).isNotNull();
  }
}
