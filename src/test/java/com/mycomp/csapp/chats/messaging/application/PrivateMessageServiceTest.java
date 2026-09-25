package com.mycomp.csapp.chats.messaging.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import com.mycomp.csapp.chats.history.event.MessageSentEvent;
import com.mycomp.csapp.chats.messaging.payload.PrivateMessagePayload;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@ExtendWith(MockitoExtension.class)
class PrivateMessageServiceTest {

  @Mock private SimpMessagingTemplate messagingTemplate;
  @Mock private ApplicationEventPublisher eventPublisher;

  private PrivateMessageService privateMessageService;

  @BeforeEach
  void setUp() {
    privateMessageService = new PrivateMessageService(messagingTemplate, eventPublisher);
  }

  @Test
  void sendPrivateMessageOverwritesSenderIdAndDeliversToRecipientQueue() {
    PrivateMessagePayload message = new PrivateMessagePayload();
    message.setRecipientId("bob@example.com");
    message.setMessage("hi bob");

    privateMessageService.sendPrivateMessage(message, "alice@example.com");

    ArgumentCaptor<PrivateMessagePayload> payloadCaptor =
        ArgumentCaptor.forClass(PrivateMessagePayload.class);
    verify(messagingTemplate)
        .convertAndSend(eq("/queue/messages.bob@example.com"), payloadCaptor.capture());

    PrivateMessagePayload delivered = payloadCaptor.getValue();
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
