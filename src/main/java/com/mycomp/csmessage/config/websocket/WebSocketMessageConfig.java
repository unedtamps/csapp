package com.mycomp.csmessage.config.websocket;

import com.mycomp.csmessage.chats.middleware.JwtHandshakeHandler;
import com.mycomp.csmessage.chats.middleware.JwtValidationInterceptor;

import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketMessageConfig implements WebSocketMessageBrokerConfigurer {

  private final JwtHandshakeHandler jwtHandshakeHandler;
  private final JwtValidationInterceptor jwtValidationInterceptor;
  private final StompRelayConfig stompRelayConfig;

  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    registry
        .addEndpoint("/ws")
        .setAllowedOriginPatterns("*")
        .addInterceptors(jwtValidationInterceptor)
        .setHandshakeHandler(jwtHandshakeHandler);
  }

  @Override
  public void configureMessageBroker(MessageBrokerRegistry registry) {
    registry.setApplicationDestinationPrefixes("/app");
    registry
        .enableStompBrokerRelay("/topic", "/queue")
        .setRelayHost(stompRelayConfig.getRelayHost())
        .setRelayPort(stompRelayConfig.getRelayPort())
        .setVirtualHost(stompRelayConfig.getVirtualHost())
        .setClientLogin(stompRelayConfig.getLogin())
        .setClientPasscode(stompRelayConfig.getPasscode())
        .setSystemLogin(stompRelayConfig.getSystemLogin())
        .setSystemPasscode(stompRelayConfig.getSystemPasscode());
    registry.setUserDestinationPrefix("/user");
  }
}
