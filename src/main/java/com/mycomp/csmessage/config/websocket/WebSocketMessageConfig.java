package com.mycomp.csmessage.config.websocket;

import com.mycomp.csmessage.chats.middleware.StompAuthInterceptor;
import com.mycomp.csmessage.config.security.CorsConfig;

import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketMessageConfig implements WebSocketMessageBrokerConfigurer {

  private final StompRelayConfig stompRelayConfig;
  private final StompAuthInterceptor stompAuthInterceptor;
  private final CorsConfig corsConfig;

  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    registry
        .addEndpoint("/ws")
        .setAllowedOriginPatterns(corsConfig.getAllowedOriginPatterns().toArray(String[]::new));
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
  }

  @Override
  public void configureClientInboundChannel(ChannelRegistration registration) {
    registration.interceptors(stompAuthInterceptor);
  }
}
