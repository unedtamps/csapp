package com.mycomp.csapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class CSApp {

  public static void main(String[] args) {
    SpringApplication.run(CSApp.class, args);
  }
}
