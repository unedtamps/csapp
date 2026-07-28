package com.mycomp.csmessage.config;

// @Configuration
// @EnableWebSocketSecurity
// public class WebSocketSecurityConfig {
//
//   @Bean
//   public AuthorizationManager<Message<?>> messageAuthorizationManager(
//       MessageMatcherDelegatingAuthorizationManager.Builder messages) {
//
//     messages
//         // 1. CONNECT, DISCONNECT, HEARTBEAT — no destination, always allowed
//         .nullDestMatcher()
//         .permitAll()
//
//         // 2. Private subscriptions require auth
//         .simpSubscribeDestMatchers("/user/**")
//         .authenticated()
//
//         // 3. App destinations require auth
//         .simpDestMatchers("/app/**")
//         .authenticated()
//
//         // 4. Everything else requires auth
//         .anyMessage()
//         .authenticated();
//
//     return messages.build();
//   }
// }
