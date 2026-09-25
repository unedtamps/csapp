package com.mycomp.csapp.config.websocket;

import lombok.Data;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "spring.rabbitmq.stomp")
@Data
public class StompRelayProperties {
  private String relayHost = "localhost";
  private int relayPort = 61613;
  private String virtualHost = "/";
  private String login = "guest";
  private String passcode = "guest";
  private String systemLogin = "guest";
  private String systemPasscode = "guest";
}
