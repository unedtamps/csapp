package com.mycomp.csapp.config.websocket;

import com.mycomp.csapp.accounts.auth.websocket.StompAuthenticationInterceptor;
import com.mycomp.csapp.config.web.CorsProperties;

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
public class WebSocketMessageBrokerConfiguration implements WebSocketMessageBrokerConfigurer {

  private final StompRelayProperties stompRelayProperties;
  private final StompAuthenticationInterceptor stompAuthenticationInterceptor;
  private final CorsProperties corsProperties;

  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    registry
        .addEndpoint("/ws")
        .setAllowedOriginPatterns(corsProperties.getAllowedOriginPatterns().toArray(String[]::new));
  }

  @Override
  public void configureMessageBroker(MessageBrokerRegistry registry) {
    registry.setApplicationDestinationPrefixes("/app");
    registry
        .enableStompBrokerRelay("/topic", "/queue")
        .setRelayHost(stompRelayProperties.getRelayHost())
        .setRelayPort(stompRelayProperties.getRelayPort())
        .setVirtualHost(stompRelayProperties.getVirtualHost())
        .setClientLogin(stompRelayProperties.getLogin())
        .setClientPasscode(stompRelayProperties.getPasscode())
        .setSystemLogin(stompRelayProperties.getSystemLogin())
        .setSystemPasscode(stompRelayProperties.getSystemPasscode());
  }

  @Override
  public void configureClientInboundChannel(ChannelRegistration registration) {
    registration.interceptors(stompAuthenticationInterceptor);
  }
}
