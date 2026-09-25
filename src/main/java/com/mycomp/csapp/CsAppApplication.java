package com.mycomp.csapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class CsAppApplication {

  public static void main(String[] args) {
    SpringApplication.run(CsAppApplication.class, args);
  }
}
