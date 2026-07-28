package com.mycomp.csmessage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class CSMessage {

  public static void main(String[] args) {
    SpringApplication.run(CSMessage.class, args);
  }
}
